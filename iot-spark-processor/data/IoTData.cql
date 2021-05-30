//Create keyspace
CREATE KEYSPACE IF NOT EXISTS EquipmentKeySpace WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

//Drop table
DROP TABLE IF EXISTS EquipmentKeySpace.Total_Equipment;
DROP TABLE IF EXISTS EquipmentKeySpace.Window_Equipment;
DROP TABLE IF EXISTS EquipmentKeySpace.Total_Equipment_batch;
DROP TABLE IF EXISTS EquipmentKeySpace.Window_Equipment_batch;

//Create table
CREATE TABLE EquipmentKeySpace.Total_Equipment (equipmentId text ,  sensorType text,  totalCount bigint,  totalSum bigint, timeStamp timestamp,recordDate text,PRIMARY KEY (equipmentId,recordDate,sensorType));
CREATE TABLE EquipmentKeySpace.Window_Equipment (equipmentId text , sensorType text,  totalCount bigint,  totalSum bigint, timeStamp timestamp,recordDate text,PRIMARY KEY (equipmentId,recordDate,sensorType));
CREATE TABLE EquipmentKeySpace.Total_Equipment_batch (equipmentId text ,  sensorType text,  totalCount bigint, timeStamp timestamp,recordDate text,PRIMARY KEY (equipmentId,recordDate,sensorType));
CREATE TABLE EquipmentKeySpace.Window_Equipment_batch (equipmentId text , sensorType text,  totalCount bigint, timeStamp timestamp,recordDate text,PRIMARY KEY (equipmentId,recordDate,sensorType));
