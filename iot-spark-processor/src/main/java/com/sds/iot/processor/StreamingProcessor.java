package com.sds.iot.processor;

import com.sds.iot.dto.IoTData;
import com.sds.iot.util.IoTDataDeserializer;
import com.sds.iot.util.PropertyFileReader;
import com.datastax.spark.connector.util.JavaApiHelper;
import com.sds.iot.streamhandler.AppendToHDFS;
import com.sds.iot.streamhandler.RealtimeTotalEquipmentData;
import com.sds.iot.streamhandler.RealtimeWindowEquipmentData;
import com.sds.iot.mlmodel.RealtimeMLModel;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.CanCommitOffsets;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.streaming.kafka010.OffsetRange;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import scala.reflect.ClassTag;

/**
 * This class consumes Kafka IoT messages and creates stream for processing the IoT data.
 *
 * @author apssouza22
 */
public class StreamingProcessor implements Serializable {

    private static final Logger logger = Logger.getLogger(StreamingProcessor.class);
    private final Properties prop;

    public StreamingProcessor(Properties properties) {
        this.prop = properties;
    }

    public static void main(String[] args) throws Exception {
        //String file = "iot-spark-local.properties";
        String file = "iot-spark.properties";
        Properties prop = PropertyFileReader.readPropertyFile(file);
        StreamingProcessor streamingProcessor = new StreamingProcessor(prop);
        streamingProcessor.start();
    }

    private void start() throws Exception {
        String[] jars = {};
        String parquetFile = prop.getProperty("com.iot.app.hdfs") + "iot-data-parquet";
        Map<String, Object> kafkaProperties = getKafkaParams(prop);
        SparkConf conf = getSparkConf(prop, jars);

        //TODO: remove when running in the cluster
        //conf.set("spark.driver.bindAddress", "127.0.0.1");

        //batch interval of 5 seconds for incoming stream
        JavaStreamingContext streamingContext = new JavaStreamingContext(conf, Durations.seconds(5));
        streamingContext.checkpoint(prop.getProperty("com.iot.app.spark.checkpoint.dir"));
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();
        Map<TopicPartition, Long> offsets = getOffsets(parquetFile, sparkSession);
        JavaInputDStream<ConsumerRecord<String, IoTData>> kafkaStream = getKafkaStream(
                prop,
                streamingContext,
                kafkaProperties,
                offsets
        );

        logger.info("Starting Stream Processing");

        //Basically we are sending the data to each worker nodes on a Spark cluster.
        StreamProcessor streamProcessor = new StreamProcessor(kafkaStream);
        streamProcessor.transform();

        //The successors are set like this: hdfs -> mlModel -> mlModel -> total -> window
        AppendToHDFS hdfs = new AppendToHDFS();
        hdfs.setSql(sparkSession);
        hdfs.setFile(parquetFile);
        RealtimeMLModel mlModel = new RealtimeMLModel();
        RealtimeTotalEquipmentData total = new RealtimeTotalEquipmentData();
        RealtimeWindowEquipmentData window = new RealtimeWindowEquipmentData();

        hdfs.setSuccessor(mlModel);
        mlModel.setSuccessor(total);
        total.setSuccessor(window);

        hdfs.processRequest(streamProcessor);

        commitOffset(kafkaStream);

        streamingContext.start();
        streamingContext.awaitTermination();
    }

    private Map<TopicPartition, Long> getOffsets(final String parquetFile, final SparkSession sparkSession) {
        try {
            LatestOffSetReader latestOffSetReader = new LatestOffSetReader(sparkSession, parquetFile);
            return latestOffSetReader.read().offsets();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * Commit the ack to kafka after process have completed
     *
     * @param directKafkaStream
     */
    private void commitOffset(JavaInputDStream<ConsumerRecord<String, IoTData>> directKafkaStream) {
        directKafkaStream.foreachRDD((JavaRDD<ConsumerRecord<String, IoTData>> equipmentRdd) -> {
            if (!equipmentRdd.isEmpty()) {
                OffsetRange[] offsetRanges = ((HasOffsetRanges) equipmentRdd.rdd()).offsetRanges();

                CanCommitOffsets canCommitOffsets = (CanCommitOffsets) directKafkaStream.inputDStream();
                canCommitOffsets.commitAsync(offsetRanges, new EquipmentOffsetCommitCallback());
            }
        });
    }


    private Map<String, Object> getKafkaParams(Properties prop) {
        Map<String, Object> kafkaProperties = new HashMap<>();
        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, prop.getProperty("com.iot.app.kafka.brokerlist"));
        kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IoTDataDeserializer.class);
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, prop.getProperty("com.iot.app.kafka.topic"));
        kafkaProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, prop.getProperty("com.iot.app.kafka.resetType"));
        kafkaProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return kafkaProperties;
    }


    private JavaInputDStream<ConsumerRecord<String, IoTData>> getKafkaStream(
            Properties prop,
            JavaStreamingContext streamingContext,
            Map<String, Object> kafkaProperties,
            Map<TopicPartition, Long> fromOffsets
    ) {
        List<String> topicSet = Arrays.asList(new String[]{prop.getProperty("com.iot.app.kafka.topic")});
        if (fromOffsets.isEmpty()) {
            return KafkaUtils.createDirectStream(
                    streamingContext,
                    LocationStrategies.PreferConsistent(),
                    ConsumerStrategies.Subscribe(topicSet, kafkaProperties)
            );
        }

        return KafkaUtils.createDirectStream(
                streamingContext,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topicSet, kafkaProperties, fromOffsets)
        );
    }

    private SparkConf getSparkConf(Properties prop, String[] jars) {
        return new SparkConf()
                .setAppName(prop.getProperty("com.iot.app.spark.app.name"))
                .setMaster(prop.getProperty("com.iot.app.spark.master"))
                .set("spark.cassandra.connection.host", prop.getProperty("com.iot.app.cassandra.host"))
                .set("spark.cassandra.connection.port", prop.getProperty("com.iot.app.cassandra.port"))
                .set("spark.cassandra.auth.username", prop.getProperty("com.iot.app.cassandra.username"))
                .set("spark.cassandra.auth.password", prop.getProperty("com.iot.app.cassandra.password"))
                .set("spark.cassandra.connection.keep_alive_ms", prop.getProperty("com.iot.app.cassandra.keep_alive"));
    }

}

final class EquipmentOffsetCommitCallback implements OffsetCommitCallback, Serializable {

    private static final Logger log = Logger.getLogger(EquipmentOffsetCommitCallback.class);

    @Override
    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
        log.info("---------------------------------------------------");
        log.info(String.format("{0} | {1}", new Object[]{offsets, exception}));
        log.info("---------------------------------------------------");
    }
}
