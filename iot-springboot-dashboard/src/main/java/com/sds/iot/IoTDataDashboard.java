package com.sds.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring boot application class for Dashboard.
 *
 * @author abaghel
 */
@SpringBootApplication
@EnableScheduling
@EnableCassandraRepositories("com.sds.iot.dao")
public class IoTDataDashboard {
    public static void main(String[] args) {
        SpringApplication.run(IoTDataDashboard.class, args);
    }
}

