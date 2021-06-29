package com.sds.iot.processor;

import static com.datastax.spark.connector.japi.CassandraStreamingJavaUtil.javaFunctions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.JavaDStream;

import com.sds.iot.dto.AggregateKey;
import com.sds.iot.dto.AggregateValue;
import com.sds.iot.entity.WindowEquipmentData;
import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.sds.iot.entity.TotalEquipmentData;
import com.sds.iot.dto.IoTData;

import scala.Tuple2;

class RealtimeWindowEquipmentData extends StreamHandler {

    private static final Logger logger = Logger.getLogger(RealtimeWindowEquipmentData.class);


    /**
     * Method to get window equipment counts of different type of sensors for each equipment. Window duration = 30 seconds
     * and Slide interval = 5 seconds
     */
    public void processRequest(StreamProcessor processor) {
        // reduce by key and window (30 sec window and 5 sec slide).
        JavaDStream<WindowEquipmentData> equipmentDStream = processor.getTransformedStream()
                .mapToPair(iot -> new Tuple2<>(new AggregateKey(iot.getEquipmentId(), iot.getSensorType()), 
                    new AggregateValue(1L, new Double(iot.getValue()).longValue())))
                .reduceByKeyAndWindow(((Function2<AggregateValue, AggregateValue, AggregateValue>) (a, b) -> 
                    new AggregateValue(a.getCount() + b.getCount(), a.getSum() + b.getSum())), 
                    Durations.seconds(30), Durations.seconds(5))
                .map(RealtimeWindowEquipmentData::mapToWindowEquipmentData);

        // Map Cassandra table column
        HashMap<String, String> columnNameMappings = new HashMap<>();
        columnNameMappings.put("equipmentId", "equipmentid");
        columnNameMappings.put("sensorType", "sensortype");
        columnNameMappings.put("totalCount", "totalcount");
        columnNameMappings.put("totalSum", "totalsum");
        columnNameMappings.put("timeStamp", "timestamp");
        columnNameMappings.put("recordDate", "recorddate");

        // call CassandraStreamingJavaUtil function to save in DB
        javaFunctions(equipmentDStream).writerBuilder(
                "equipmentkeyspace",
                "window_equipment",
                CassandraJavaUtil.mapToRow(WindowEquipmentData.class, columnNameMappings)
        ).saveToCassandra();

        if(successor != null) successor.processRequest(processor);
    }

    /**
     * Function to create WindowEquipmentData object from IoT data
     *
     * @param tuple
     * @return
     */
    private static WindowEquipmentData mapToWindowEquipmentData(Tuple2<AggregateKey, AggregateValue> tuple) {
        logger.info("Window Count : " +
                "key " + tuple._1().getEquipmentId() + "-" + tuple._1().getSensorType() +
                " value " + tuple._2().getCount() + "," + tuple._2().getSum());

        WindowEquipmentData equipmentData = new WindowEquipmentData();
        equipmentData.setEquipmentId(tuple._1().getEquipmentId());
        equipmentData.setSensorType(tuple._1().getSensorType());
        equipmentData.setTotalCount(tuple._2().getCount());
        equipmentData.setTotalSum(tuple._2().getSum());
        equipmentData.setTimeStamp(new Date());
        equipmentData.setRecordDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        return equipmentData;
    }
}