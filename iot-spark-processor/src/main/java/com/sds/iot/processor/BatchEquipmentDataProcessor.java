package com.sds.iot.processor;

import com.sds.iot.dto.AggregateKey;
import com.sds.iot.entity.WindowEquipmentData;
import com.sds.iot.util.IotDataTimestampComparator;
import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.sds.iot.entity.TotalEquipmentData;
import com.sds.iot.dto.IoTData;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;

import scala.Tuple2;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class to process IoT data stream and to produce equipment data details.
 */
public class BatchEquipmentDataProcessor {
    private static final Logger logger = Logger.getLogger(BatchEquipmentDataProcessor.class);

    /**
     * Method to get total equipment counts of different type of sensor for each equipment.
     *
     * @param filteredIotDataStream IoT data stream
     */
    public static void processTotalEquipmentData(JavaRDD<IoTData> filteredIotDataStream) {
        // We need to get count of sensor group by equipmentId and sensorType
        JavaPairRDD<AggregateKey, Long> countDStreamPair = filteredIotDataStream
                .mapToPair(iot -> new Tuple2<>(
                        new AggregateKey(iot.getEquipmentId(), iot.getSensorType()),
                        1L
                ))
                .reduceByKey((a, b) -> a + b);

        JavaRDD<TotalEquipmentData> equipmentDStream = countDStreamPair
                .map(BatchEquipmentDataProcessor::transformToTotalEquipmentData);

        persistTotalEquipment(equipmentDStream);
    }

    private static void persistTotalEquipment(JavaRDD<TotalEquipmentData> equipmentDStream) {
        // Map Cassandra table column
        Map<String, String> columnNameMappings = new HashMap<String, String>();
        columnNameMappings.put("equipmentId", "equipmentid");
        columnNameMappings.put("sensorType", "sensortype");
        columnNameMappings.put("totalCount", "totalcount");
        columnNameMappings.put("timeStamp", "timestamp");
        columnNameMappings.put("recordDate", "recorddate");

        CassandraJavaUtil.javaFunctions(equipmentDStream).writerBuilder(
                "equipmentkeyspace",
                "total_equipment_batch",
                CassandraJavaUtil.mapToRow(TotalEquipmentData.class, columnNameMappings)
        ).saveToCassandra();
    }


    /**
     * Method to get window equipment counts of different type of sensors for each equipment. Window duration = 30 seconds and Slide interval = 10 seconds
     *
     * @param filteredIotDataStream IoT data stream
     */
    public static void processWindowEquipmentData(JavaRDD<IoTData> filteredIotDataStream) {
        Date minTimestamp = filteredIotDataStream.min(new IotDataTimestampComparator()).getTimestamp();
        Date maxTimestamp = filteredIotDataStream.max(new IotDataTimestampComparator()).getTimestamp();
        long diffInMillies = Math.abs(minTimestamp.getTime() - maxTimestamp.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        Calendar c = Calendar.getInstance();
        c.setTime(minTimestamp);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        Date start = c.getTime();
        for (int i = 0; i < diff; i++) {
            c.setTime(start);
            c.add(Calendar.DATE, 1);
            Date end = c.getTime();
            processInterval(filteredIotDataStream, start, end);
            start = end;
        }
    }

    private static void processInterval(JavaRDD<IoTData> data, Date start, Date end) {
        JavaRDD<IoTData> filteredData = filterByTime(data, start, end);
        JavaRDD<WindowEquipmentData> equipmentDStream = getWindowEquipmentData(filteredData);
        persistWindowEquipment(equipmentDStream);
    }

    private static void persistWindowEquipment(JavaRDD<WindowEquipmentData> equipmentDStream) {
        // Map Cassandra table column
        Map<String, String> columnNameMappings = new HashMap<>();
        columnNameMappings.put("equipmentId", "equipmentid");
        columnNameMappings.put("sensorType", "sensortype");
        columnNameMappings.put("totalCount", "totalcount");
        columnNameMappings.put("timeStamp", "timestamp");
        columnNameMappings.put("recordDate", "recorddate");

        // call CassandraStreamingJavaUtil function to save in DB
        CassandraJavaUtil.javaFunctions(equipmentDStream).writerBuilder(
                "equipmentkeyspace",
                "window_equipment_batch",
                CassandraJavaUtil.mapToRow(WindowEquipmentData.class, columnNameMappings)
        ).saveToCassandra();
    }

    private static JavaRDD<WindowEquipmentData> getWindowEquipmentData(JavaRDD<IoTData> filteredData) {
        JavaPairRDD<AggregateKey, Long> javaPairRDD = filteredData.mapToPair(iot -> new Tuple2<>(
                new AggregateKey(iot.getEquipmentId(), iot.getSensorType()),
                1L
        ));

        // Transform to dstream of EquipmentData
        return javaPairRDD.map(windowEquipmentDataFunc);
    }

    /**
     * Filter the data in a given time period
     *
     * @param data  | The dataset of data
     * @param start | Start of the time period
     * @param end   | End of the time period
     * @return A set of data in the given time period
     */
    private static JavaRDD<IoTData> filterByTime(JavaRDD<IoTData> data, Date start, Date end) {
        return data.filter(measurement -> (
                        measurement.getTimestamp().equals(start) || measurement.getTimestamp().after(start)
                ) && measurement.getTimestamp().before(end)
        );
    }

    //Function to create TotalEquipmentData object from IoT data
    private static final TotalEquipmentData transformToTotalEquipmentData(Tuple2<AggregateKey, Long> tuple) {
        logger.debug("Total Count : " + "key " + tuple._1().getEquipmentId() + "-" + tuple._1().getSensorType() + " value " + tuple._2());
        TotalEquipmentData equipmentData = new TotalEquipmentData();
        equipmentData.setEquipmentId(tuple._1().getEquipmentId());
        equipmentData.setSensorType(tuple._1().getSensorType());
        equipmentData.setTotalCount(tuple._2());
        equipmentData.setTimeStamp(new Date());
        equipmentData.setRecordDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        return equipmentData;
    };

    //Function to create WindowEquipmentData object from IoT data
    private static final Function<Tuple2<AggregateKey, Long>, WindowEquipmentData> windowEquipmentDataFunc = (tuple -> {
        logger.debug("Window Count : " + "key " + tuple._1().getEquipmentId() + "-" + tuple._1().getSensorType() + " value " + tuple._2());
        WindowEquipmentData equipmentData = new WindowEquipmentData();
        equipmentData.setEquipmentId(tuple._1().getEquipmentId());
        equipmentData.setSensorType(tuple._1().getSensorType());
        equipmentData.setTotalCount(tuple._2());
        equipmentData.setTimeStamp(new Date());
        equipmentData.setRecordDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        return equipmentData;
    });
}
