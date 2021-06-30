package com.sds.iot.mlmodel;

public class MLModel {
    private MLModelStrategy strategy;

    public MLModel(MLModelStrategy strategy) {
        this.strategy = strategy;
    }

    public void action() {
        strategy.action();
    }

    public void setBehavior(MLModelStrategy strategy) {
        this.strategy = strategy;
    }
}