package com.sds.iot.mlmodel;

import org.apache.log4j.Logger;
import com.sds.iot.processor.StreamHandler;
import com.sds.iot.processor.StreamProcessor;

public class RealtimeMLModel extends StreamHandler {

    private static final Logger logger = Logger.getLogger(RealtimeMLModel.class);

    public void processRequest(StreamProcessor processor) {

        logger.info("Run Machine Learning Model");

        MLModel abnormalModel = new MLModel(new AbnormalDetectorA());
        abnormalModel.action();
        
        MLModel predictPartLife = new MLModel(new PredictPartLifeA());
        predictPartLife.action();

        abnormalModel.setBehavior(new AbnormalDetectorB());
        abnormalModel.action();

        predictPartLife.setBehavior(new PredictPartLifeB());
        predictPartLife.action();

        if(successor != null) successor.processRequest(processor);
    }
}