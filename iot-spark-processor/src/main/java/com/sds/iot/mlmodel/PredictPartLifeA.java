package com.sds.iot.mlmodel;

import org.apache.log4j.Logger;

public class PredictPartLifeA implements MLModelStrategy {
    private static final Logger logger = Logger.getLogger(PredictPartLifeA.class);

    @Override public void action() {
        logger.info("PredictPartLifeA is running");
    }
}