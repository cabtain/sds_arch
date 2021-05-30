package com.sds.iot.dto;

import java.io.Serializable;
import java.sql.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Class to represent the IoT equipment data.
 */
public class IoTData implements Serializable{
	
	private String eventId;
	private String equipmentId;
	private String sensorType;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone="MST")
	private Date timestamp;
	private double value;
    private Map<String, String> metaData;
	
	public IoTData(){
		
	}
	
	public IoTData(String eventId, String equipmentId, String sensorType, Date timestamp, double value) {
		super();
		this.eventId = eventId;
		this.equipmentId = equipmentId;
		this.sensorType = sensorType;
		this.timestamp = timestamp;
		this.value = value;
	}

	public String getEventId() {
		return eventId;
	}

	public String getEquipmentId() {
		return equipmentId;
	}

	public String getSensorType() {
		return sensorType;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public double getValue() {
		return value;
	}

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }
}
