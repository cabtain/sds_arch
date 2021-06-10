package com.sds.iot.processor;

import com.sds.iot.dto.IoTData;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaMapWithStateDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.OffsetRange;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import scala.Tuple2;

public class StreamProcessor implements Serializable {

    private static final Logger logger = Logger.getLogger(StreamProcessor.class);

    final JavaDStream<ConsumerRecord<String, IoTData>> directKafkaStream;
    private JavaDStream<IoTData> transformedStream;

    public StreamProcessor(JavaDStream<ConsumerRecord<String, IoTData>> directKafkaStream) {
        this.directKafkaStream = directKafkaStream;
    }

    private static JavaRDD<IoTData> transformRecord(JavaRDD<ConsumerRecord<String, IoTData>> item) {
        OffsetRange[] offsetRanges;
        offsetRanges = ((HasOffsetRanges) item.rdd()).offsetRanges();
        return item.mapPartitionsWithIndex(addMetaData(offsetRanges), true);
    }

    private static Function2<Integer, Iterator<ConsumerRecord<String, IoTData>>, Iterator<IoTData>> addMetaData(
            final OffsetRange[] offsetRanges
    ) {
        return (index, items) -> {
            List<IoTData> list = new ArrayList<>();
            while (items.hasNext()) {
                ConsumerRecord<String, IoTData> next = items.next();
                IoTData dataItem = next.value();

                Map<String, String> meta = new HashMap<>();
                meta.put("topic", offsetRanges[index].topic());
                meta.put("fromOffset", "" + offsetRanges[index].fromOffset());
                meta.put("kafkaPartition", "" + offsetRanges[index].partition());
                meta.put("untilOffset", "" + offsetRanges[index].untilOffset());
                meta.put("dayOfWeek", "" + dataItem.getTimestamp().toLocalDate().getDayOfWeek().getValue());

                dataItem.setMetaData(meta);
                list.add(dataItem);
            }
            return list.iterator();
        };
    }

    public StreamProcessor transform() {
        this.transformedStream = directKafkaStream.transform(StreamProcessor::transformRecord);
        return this;
    }

    public StreamProcessor appendToHDFS(final SparkSession sql, final String file) {
        transformedStream.foreachRDD(rdd -> {
                    if (rdd.isEmpty()) {
                        return;
                    }
                    Dataset<Row> dataFrame = sql.createDataFrame(rdd, IoTData.class);
                    Dataset<Row> dfStore = dataFrame.selectExpr(
                            "equipmentId", "value", "timestamp", "eventId", "sensorType",
                            "metaData.fromOffset as fromOffset",
                            "metaData.untilOffset as untilOffset",
                            "metaData.kafkaPartition as kafkaPartition",
                            "metaData.topic as topic",
                            "metaData.dayOfWeek as dayOfWeek"
                    );
                    dfStore.printSchema();
                    dfStore.write()
                            .partitionBy("topic", "kafkaPartition", "dayOfWeek")
                            .mode(SaveMode.Append)
                            .parquet(file);
                }
        );
        return this;
    }

    public StreamProcessor processTotalEquipmentData() {
        RealtimeEquipmentDataProcessor.processTotalEquipmentData(transformedStream);
        return this;
    }

    public StreamProcessor processWindowEquipmentData() {
        RealtimeEquipmentDataProcessor.processWindowEquipmentData(transformedStream);
        return this;
    }
}
