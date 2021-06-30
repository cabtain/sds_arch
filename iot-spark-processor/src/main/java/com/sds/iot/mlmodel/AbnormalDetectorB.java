package com.sds.iot.mlmodel;

import org.apache.log4j.Logger;

public class AbnormalDetectorB implements MLModelStrategy {
    private static final Logger logger = Logger.getLogger(AbnormalDetectorB.class);

    @Override public void action() {
        logger.info("AbnormalDetectorB is running");
    }
}