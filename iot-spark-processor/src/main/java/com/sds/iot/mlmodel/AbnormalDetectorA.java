package com.sds.iot.mlmodel;

import org.apache.log4j.Logger;

public class AbnormalDetectorA implements MLModelStrategy {
    private static final Logger logger = Logger.getLogger(AbnormalDetectorA.class);

    @Override public void action() {
        logger.info("AbnormalDetectorA is running");
    }
}