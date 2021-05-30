package com.sds.kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * IoT data event producer class which uses Kafka producer to send iot data events to Kafka
 */
public class IoTDataProducer {

    private static final Logger logger = Logger.getLogger(IoTDataProducer.class);
    private final Producer<String, IoTData> producer;

    public IoTDataProducer(final Producer<String, IoTData> producer) {
        this.producer = producer;
    }

    public static void main(String[] args) throws Exception {
        Properties properties = PropertyFileReader.readPropertyFile();
        Producer<String, IoTData> producer = new Producer<>(new ProducerConfig(properties));
        IoTDataProducer iotProducer = new IoTDataProducer(producer);
        iotProducer.generateIoTEvent(properties.getProperty("kafka.topic"));
    }

    /**
     * Method runs in while loop and generates random IoT data in JSON with below format.
     * <p>
     * {"eventId":"52f08f03-cd14-411a-8aef-ba87c9a99997","sensorType":"Temperature","routeId":"Equipment_A","timestamp":1465471124373,"value":80.0}
     *
     * @throws InterruptedException
     */
    private void generateIoTEvent(String topic) throws InterruptedException {
        List<String> equipmentList = Arrays.asList(
                new String[]{"Equipment_A", "Equipment_B", "Equipment_C"}
        );
        List<String> sensorTypeList = Arrays.asList(
                new String[]{"Temperature", "Current", "Voltage", "Vibration", "Level"}
        );
        Random rand = new Random();
        logger.info("Sending events");

        while (true) {
            IoTData event = generateEquipmentData(equipmentList, sensorTypeList, rand);
            producer.send(new KeyedMessage<>(topic, event));
            Thread.sleep(rand.nextInt(300 - 100) + 100);//random delay of 0.1 to 0.3 seconds
        }
    }

    private IoTData generateEquipmentData(
            final List<String> equipmentList,
            final List<String> sensorTypeList,
            final Random rand
    ) {
        String eventId = UUID.randomUUID().toString();
        String equipmentId = equipmentList.get(rand.nextInt(3));
        String sensorType = sensorTypeList.get(rand.nextInt(5));
        Date timestamp = new Date();
        double value = rand.nextInt(100 - 10) + 10;// random speed between 10 to 100

        IoTData event = new IoTData(
                eventId,
                equipmentId,
                sensorType,
                timestamp,
                value
        );

        return event;
    }
}
