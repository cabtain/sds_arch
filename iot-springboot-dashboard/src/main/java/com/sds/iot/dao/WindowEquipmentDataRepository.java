package com.sds.iot.dao;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.sds.iot.dao.entity.WindowEquipmentData;

import java.util.UUID;

/**
 * DAO class for window_equipment 
 */
@Repository
public interface WindowEquipmentDataRepository extends CassandraRepository<WindowEquipmentData,UUID>{
	
	@Query("SELECT * FROM equipmentkeyspace.window_equipment WHERE recorddate = ?0 ALLOW FILTERING")
	Iterable<WindowEquipmentData> findEquipmentDataByDate(String date);

}
