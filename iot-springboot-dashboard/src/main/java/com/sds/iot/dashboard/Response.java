package com.sds.iot.dashboard;

import java.io.Serializable;
import java.util.List;

import com.sds.iot.dao.entity.TotalEquipmentData;
import com.sds.iot.dao.entity.WindowEquipmentData;

/**
 * Response object containing equipment details that will be sent to dashboard.
 */
public class Response implements Serializable {
    private List<TotalEquipmentData> totalEquipment;
    private List<WindowEquipmentData> windowEquipment;

    public List<TotalEquipmentData> getTotalEquipment() {
        return totalEquipment;
    }

    public void setTotalEquipment(List<TotalEquipmentData> totalEquipment) {
        this.totalEquipment = totalEquipment;
    }

    public List<WindowEquipmentData> getWindowEquipment() {
        return windowEquipment;
    }

    public void setWindowEquipment(List<WindowEquipmentData> windowEquipment) {
        this.windowEquipment = windowEquipment;
    }
}
