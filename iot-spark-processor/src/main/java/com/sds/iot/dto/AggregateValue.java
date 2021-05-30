package com.sds.iot.dto;

import java.io.Serializable;

/**
 * Value class for calculation
 */
public class AggregateValue implements Serializable {

    private Long count;
    private Long sum;

    public AggregateValue(Long count, Long sum) {
        super();
        this.count = count;
        this.sum = sum;
    }

    public Long getCount() {
        return count;
    }

    public Long getSum() {
        return sum;
    }
}
