#!/bin/sh

########################################################################
# title:          Build Complete Project
# author:         Alexsandro souza (https://apssouza.com.br)
# url:            https://github.com/apssouza22
# description:    Build complete Big data pipeline
# usage:          sh ./project-orchestrate.sh
########################################################################

#set -ex

# Create casandra schema
docker exec cassandra-iot cqlsh --username cassandra --password cassandra  -f /schema.cql
# Delete Kafka topic "iot-data-event"
docker exec kafka-iot kafka-topics --delete --zookeeper zookeeper:2181 --topic iot-data-event
# Create Kafka topic "iot-data-event"
docker exec kafka-iot kafka-topics --create --topic iot-data-event --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181
# Install libc6-compat lib in both sparks containers
#docker exec spark-master apk add --no-cache libc6-compat
#docker exec spark-worker-1 apk add --no-cache libc6-compat
# Delete our folders on Hadoop file system
docker exec namenode hdfs dfs -rm -r /sds-arch
# Create our folders on Hadoop file system and total permission to those
docker exec namenode hdfs dfs -mkdir /sds-arch
docker exec namenode hdfs dfs -mkdir /sds-arch/checkpoint
docker exec namenode hdfs dfs -mkdir /sds-arch/iot-data-parquet
docker exec namenode hdfs dfs -chmod -R 777 /sds-arch
docker exec namenode hdfs dfs -chmod -R 777 /sds-arch/checkpoint
docker exec namenode hdfs dfs -chmod -R 777 /sds-arch/iot-data-parquet


