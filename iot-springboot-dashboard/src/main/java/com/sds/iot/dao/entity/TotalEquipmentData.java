package com.sds.iot.dao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity class for total_equipment db table
 * 
 * @author abaghel
 *
 */
@Table("total_equipment")
public class TotalEquipmentData implements Serializable{
	@PrimaryKeyColumn(name = "equipmentid",ordinal = 0,type = PrimaryKeyType.PARTITIONED)
	private String equipmentId;
	@PrimaryKeyColumn(name = "recordDate",ordinal = 1,type = PrimaryKeyType.CLUSTERED)
	private String recordDate;
	@PrimaryKeyColumn(name = "sensortype",ordinal = 2,type = PrimaryKeyType.CLUSTERED)
	private String sensorType;
	@Column(value = "totalcount")
	private long totalCount;
	@Column(value = "totalsum")
	private long totalSum;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone="MST")
	@Column(value = "timestamp")
	private Date timeStamp;
	
	public String getEquipmentId() {
		return equipmentId;
	}
	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}
	public String getRecordDate() {
		return recordDate;
	}
	public void setRecordDate(String recordDate) {
		this.recordDate = recordDate;
	}
	public String getSensorType() {
		return sensorType;
	}
	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}
	public long getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}
	public long getTotalSum() {
		return totalSum;
	}
	public void setTotalSum(long totalSum) {
		this.totalSum = totalSum;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	@Override
	public String toString() {
		return "EquipmentData [equipmentId=" + equipmentId + ", sensorType=" + sensorType + ", totalCount=" + totalCount
				+ ", timeStamp=" + timeStamp + "]";
	}
}
