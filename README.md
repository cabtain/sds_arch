# SDS Architect(Lambda architecture)


Our project receives real-time IoT Data Events coming from the factory equipments, 
then ingested to Spark through Kafka. Using the Spark streaming API, we processed and analysed 
IoT data events and transformed them into some information.
While simultaneously the data is also stored into HDFS for Batch processing. 
We performed a series of stateless and stateful transformation using Spark streaming API on 
streams and persisted them to Cassandra database tables. In order to get accurate views, 
we also perform a batch processing and generating a batch view into Cassandra.
We developed responsive web equipments monitoring dashboard using Spring Boot, 
SockJs and Bootstrap which get the views from the Cassandra database and push to the UI using web socket.


All component parts are dynamically managed using Docker, which means you don't need to worry 
about setting up your local environment, the only thing you need is to have Docker installed.

System stack:
- Java 8
- Maven
- ZooKeeper
- Kafka
- Cassandra
- Spark
- Docker
- HDFS


The streaming part of the project was been referenced from iot-traffic-project [InfoQ](https://www.infoq.com/articles/traffic-data-monitoring-iot-kafka-and-spark-streaming)


### git clone

`$ sudo su -`

`$ git clone https://github.com/cabtain/sds_arch.git`

`$ cd sds_arch/`

Set the KAFKA_ADVERTISED_LISTENERS with your IP(ex. Google GCP internal IP) in the docker-compose.yml

### install maven & build

`$ apt install maven`

`$ mvn package`


### install docker-compose & run

`$ snap install docker`

`$ docker-compose -p lambda up --build`

Wait all services be up and running, then launch another terminal and run below

`$ ./project-orchestrate.sh`

### Run realtime job

`$ docker exec spark-master /spark/bin/spark-submit --class com.sds.iot.processor.StreamingProcessor --master spark://spark-master:7077 /opt/spark-data/iot-spark-processor-1.0.0.jar`

### Run the data producer

`$ java -jar iot-kafka-producer/target/iot-kafka-producer-1.0.0.jar`

### Run the service layer (Web app)

`$ java -jar iot-springboot-dashboard/target/iot-springboot-dashboard-1.0.0.jar`

Access the dashboard with the data http://34.145.64.35:3000/

### Run batch job

`$ docker exec spark-master /spark/bin/spark-submit --class com.sds.iot.processor.BatchProcessor --master spark://spark-master:7077 /opt/spark-data/iot-spark-processor-1.0.0.jar`

http://34.145.64.35:3000/

![image](https://user-images.githubusercontent.com/1121859/120259825-9a704580-c2cf-11eb-8602-974a85595851.png)


http://34.145.64.35:8080/

![image](https://user-images.githubusercontent.com/1121859/121501746-77a20780-ca1a-11eb-8690-8c6f0074d595.png)


http://34.145.64.35:8081/

![image](https://user-images.githubusercontent.com/1121859/121501876-97d1c680-ca1a-11eb-97b9-ed7e0f6a8c0e.png)


http://34.145.64.35:4040/jobs/

![image](https://user-images.githubusercontent.com/1121859/121502028-ba63df80-ca1a-11eb-9a18-f68f76e25c3f.png)

![image](https://user-images.githubusercontent.com/1121859/121502107-d071a000-ca1a-11eb-8cec-a9bb71b87ad0.png)


http://34.145.64.35:4040/streaming/

![image](https://user-images.githubusercontent.com/1121859/121502222-eda66e80-ca1a-11eb-8871-4d708fa63213.png)


http://34.145.64.35:4040/streaming/batch/?id=1623317905000

![image](https://user-images.githubusercontent.com/1121859/121502543-39591800-ca1b-11eb-9143-d09860a9bfad.png)

http://34.145.64.35:4040/jobs/job/?id=350

![image](https://user-images.githubusercontent.com/1121859/121502875-850bc180-ca1b-11eb-9b72-cff086727c27.png)

![image](https://user-images.githubusercontent.com/1121859/121502981-9c4aaf00-ca1b-11eb-99d1-9267a77c59ae.png)

http://34.145.64.35:4040/stages/

![image](https://user-images.githubusercontent.com/1121859/121503154-c308e580-ca1b-11eb-96b8-901dad56adf8.png)

http://34.145.64.35:4040/stages/stage/?id=2297&attempt=0

![image](https://user-images.githubusercontent.com/1121859/121503329-f0559380-ca1b-11eb-96fb-6ef05b5c68ca.png)

http://34.145.64.35:4040/storage/

![image](https://user-images.githubusercontent.com/1121859/121503552-2266f580-ca1c-11eb-91b4-bf56b13eb87c.png)

http://34.145.64.35:4040/storage/rdd/?id=3463

![image](https://user-images.githubusercontent.com/1121859/121503667-3f032d80-ca1c-11eb-92b7-84620f97d665.png)

http://34.145.64.35:4040/environment/

![image](https://user-images.githubusercontent.com/1121859/121503893-74a81680-ca1c-11eb-83b3-83b02ebb618b.png)

http://34.145.64.35:4040/executors/

![image](https://user-images.githubusercontent.com/1121859/121503986-8ee1f480-ca1c-11eb-924f-65e12f23c6bf.png)


http://34.145.64.35:50070/

![image2021-4-6_12-50-19](https://user-images.githubusercontent.com/1121859/118781268-d8309f80-b8c7-11eb-97a8-305c2fb902e1.png)

http://34.145.64.35:50070/explorer.html#/sds-arch/iot-data-parquet

![image2021-4-6_12-51-17](https://user-images.githubusercontent.com/1121859/118781346-eda5c980-b8c7-11eb-8e96-145ac512b9f5.png)

http://34.145.64.35:50070/dfshealth.html#tab-datanode

![image2021-4-6_12-51-55](https://user-images.githubusercontent.com/1121859/118781408-01513000-b8c8-11eb-9076-902671c15892.png)

http://34.145.64.35:50075/datanode.html

![image2021-4-6_12-52-26](https://user-images.githubusercontent.com/1121859/118781502-175ef080-b8c8-11eb-89ca-e8d12ac087c4.png)




