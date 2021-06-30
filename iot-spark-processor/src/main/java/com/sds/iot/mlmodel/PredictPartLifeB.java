package com.sds.iot.mlmodel;

import org.apache.log4j.Logger;

public class PredictPartLifeB implements MLModelStrategy {
    private static final Logger logger = Logger.getLogger(PredictPartLifeB.class);

    @Override public void action() {
        logger.info("PredictPartLifeB is running");
    }
}