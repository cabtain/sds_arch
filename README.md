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


![image2021-4-6_12-42-20](https://user-images.githubusercontent.com/1121859/118780764-56d90d00-b8c7-11eb-840e-54743d910e05.png)

http://34.145.64.35:8081/

![image2021-4-6_12-43-4](https://user-images.githubusercontent.com/1121859/118780839-6eb09100-b8c7-11eb-888e-82c641393951.png)

http://34.145.64.35:4040/jobs/

![image2021-4-6_12-44-13](https://user-images.githubusercontent.com/1121859/118780952-89830580-b8c7-11eb-9fe2-648ce2624849.png)

http://34.145.64.35:4040/streaming/

![image](https://user-images.githubusercontent.com/1121859/120272546-3d34be00-c2e8-11eb-9070-04b128cd821c.png)

http://34.145.64.35:4040/streaming/batch/?id=1617680605000

![image2021-4-6_12-45-1](https://user-images.githubusercontent.com/1121859/118781094-a6b7d400-b8c7-11eb-96c7-a1de5cdfee99.png)

http://34.145.64.35:4040/jobs/job/?id=821

![image](https://user-images.githubusercontent.com/1121859/120272742-81c05980-c2e8-11eb-9c59-4e5432246add.png)

http://34.145.64.35:4040/stages/stage/?id=2595&attempt=0

![image](https://user-images.githubusercontent.com/1121859/120272406-0363b780-c2e8-11eb-9475-5f14cfff92e8.png)

http://34.145.64.35:50070/

![image2021-4-6_12-50-19](https://user-images.githubusercontent.com/1121859/118781268-d8309f80-b8c7-11eb-97a8-305c2fb902e1.png)

http://34.145.64.35:50070/explorer.html#/sds-arch/iot-data-parque

![image2021-4-6_12-51-17](https://user-images.githubusercontent.com/1121859/118781346-eda5c980-b8c7-11eb-8e96-145ac512b9f5.png)

http://34.145.64.35:50070/dfshealth.html#tab-datanode

![image2021-4-6_12-51-55](https://user-images.githubusercontent.com/1121859/118781408-01513000-b8c8-11eb-9076-902671c15892.png)

http://34.145.64.35:50075/datanode.html

![image2021-4-6_12-52-26](https://user-images.githubusercontent.com/1121859/118781502-175ef080-b8c8-11eb-89ca-e8d12ac087c4.png)




