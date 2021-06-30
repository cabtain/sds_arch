package com.sds.iot.processor;

import java.io.Serializable;

public abstract class StreamHandler  implements Serializable{
    protected StreamHandler successor;

    public void setSuccessor(StreamHandler successor) {
        this.successor = successor;
    }

    abstract public void processRequest(StreamProcessor processor);
}