package com.sds.iot.dto;

import java.io.Serializable;

/**
 * Key class for calculation
 */
public class AggregateKey implements Serializable {

    private String equipmentId;
    private String sensorType;

    public AggregateKey(String equipmentId, String sensorType) {
        super();
        this.equipmentId = equipmentId;
        this.sensorType = sensorType;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getSensorType() {
        return sensorType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((equipmentId == null) ? 0 : equipmentId.hashCode());
        result = prime * result + ((sensorType == null) ? 0 : sensorType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AggregateKey) {
            AggregateKey other = (AggregateKey) obj;
            if (other.getEquipmentId() != null && other.getSensorType() != null) {
                if ((other.getEquipmentId().equals(this.equipmentId)) && (other.getSensorType().equals(this.sensorType))) {
                    return true;
                }
            }
        }
        return false;
    }


}
