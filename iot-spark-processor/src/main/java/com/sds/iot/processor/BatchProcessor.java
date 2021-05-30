package com.sds.iot.processor;

import com.sds.iot.dto.IoTData;
import com.sds.iot.util.PropertyFileReader;
import com.datastax.spark.connector.util.JavaApiHelper;

import org.apache.spark.SparkConf;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Properties;

import scala.reflect.ClassTag;

/**
 * Class responsible to start the process from the parque file
 */
public class BatchProcessor {


    public static void main(String[] args) throws Exception {
        var prop = PropertyFileReader.readPropertyFile("iot-spark.properties");
        String[] jars = {prop.getProperty("com.iot.app.jar")};
        var file = prop.getProperty("com.iot.app.hdfs") + "iot-data-parque";
        var conf = getSparkConfig(prop, jars);
        var sparkSession = SparkSession.builder().config(conf).getOrCreate();

        var dataFrame = getDataFrame(sparkSession, file);
        var rdd = dataFrame.javaRDD().map(BatchProcessor::transformToIotData);
        BatchEquipmentDataProcessor.processTotalEquipmentData(rdd);
        BatchEquipmentDataProcessor.processWindowEquipmentData(rdd);
        sparkSession.close();
        sparkSession.stop();
    }

    private static  IoTData transformToIotData(Row row) {
        return new IoTData(
                    row.getString(3),
                    row.getString(0),
                    row.getString(4),
                    row.getDate(2),
                    row.getDouble(1)
            );
    }

    public static Dataset<Row> getDataFrame(SparkSession sqlContext, String file) {
        return sqlContext.read()
                .parquet(file);
    }

    private static SparkConf getSparkConfig(Properties prop, String[] jars) {
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

