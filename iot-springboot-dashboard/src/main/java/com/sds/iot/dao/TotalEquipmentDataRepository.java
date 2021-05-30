package com.sds.iot.dao;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.sds.iot.dao.entity.TotalEquipmentData;

import java.util.UUID;

/**
 * DAO class for total_equipment 
 */
@Repository
public interface TotalEquipmentDataRepository extends CassandraRepository<TotalEquipmentData, UUID>{

	@Query("SELECT * FROM equipmentkeyspace.total_equipment WHERE recorddate = ?0 ALLOW FILTERING")
	Iterable<TotalEquipmentData> findEquipmentDataByDate(String date);
}
