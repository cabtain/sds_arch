package com.sds.iot.streamhandler;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import com.sds.iot.dto.IoTData;
import com.sds.iot.processor.StreamProcessor;
import com.sds.iot.processor.StreamHandler;

public class AppendToHDFS extends StreamHandler {

    SparkSession sql;
    String file;

    public void setSql(SparkSession sql) {
        this.sql = sql;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void processRequest(StreamProcessor processor) {
        processor.getTransformedStream().foreachRDD(rdd -> {
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

        if(successor != null) successor.processRequest(processor);
    }
}