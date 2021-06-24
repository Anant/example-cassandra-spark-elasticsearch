# Cassandra, Spark and Elasticsearch

This project is primarily a spark job written in Scala and built with SBT in the form of a fat jar with SBT assembly. It uses DSE Cassandra with Analytics (for Spark) and Elasticsearch, both running in separate docker containers but in the same docker-compose network, and performs three tasks located in three scala class files. 

Once a fat jar is built, it is submitted to spark (with spark-submit) with a different class name corresponding to the three scala classes located in the code and performs three different tasks: 
1. Reading a .CSV file into a SparkSQL Dataframe and saving it to Cassandra
2. Loading data from a Cassandra table into a SparkSQL Dataframe and saving that data into Elasticsearch
3. Loading data from Elasticsearch into a SparkSQL Dataframe

- - - 

## Software involved
- docker (docker-compose)
- Elasticsearch (7.13.0)
- Scala (2.11.12)
- DSE Server (6.7.7)
- Apache Spark, SparkSQL (2.2.3)

## Requirements
- docker, docker-compose
- sbt

## Table of Contents
1. [Run containers with docker-compose](#1-run-docker-containers)
2. [Setup Cassandra Table](#2-setup-cassandra-table)
3. [Perform first job (Read CSV, save to Cassandra)](#3-run-first-job)
4. [Perform second job (Read from Cassandra, save to ES)](#4-run-second-job)
5. [Perform third job (Read from ES)](#5-run-third-job)

## 1. Run Docker Containers
Make sure you are in the root folder of the repository. Run the following command: 
```bash
docker-compose up -d
```
After a minute or two, run the following command to make sure that both containers are up (both elasticsearch and dse server): 
```bash
docker ps -a
```

## 2. Setup Cassandra Table
Use the following command to setup the test Cassandra table: 
```bash
docker-compose exec dse1 cqlsh -f /app/test-data/keyspace.cql
```
Additionally, the fat jar needs to be built. Execute the following command in the root directory of the project: 
```bash
sbt assembly
```

## 3. Run First Job
This first job will read data.csv (located in /test-data/) into a SparkSQL Dataframe and then save it to DSE Cassandra. 
```bash
docker-compose exec dse1 dse spark-submit \
    --master dse://dse1 \
    --jars /jars/test-project-name-assembly-0.1.jar \
    --class "cassandraScalaEs.LoadIntoCass" \
    --conf "spark.driver.extraJavaOptions=-Dlogback.configurationFile=/app/src/test/resources/logback.xml" \
    --files /app/test-data/data.csv \
    /jars/test-project-name-assembly-0.1.jar
```

To test that the data is saved into Cassandra, see Second Job. 

## 4. Run Second Job
This second job will read data from DSE Cassandra that was inserted in the first job into a SparkSQL Dataframe. Afterwards, it will save that data to Elasticsearch. 
If the second job worked properly, then this step will run and the resulting data (being read from DSE Cassandra) will display in the console. 
Additionally, right after the non-filtered data is shown, a filtered version of the dataframe will also show (only users with id > 1). 
```bash
docker-compose exec dse1 dse spark-submit \
    --master dse://dse1 \
    --jars /jars/test-project-name-assembly-0.1.jar \
    --class "cassandraScalaEs.CassToEs" \
    --conf "spark.driver.extraJavaOptions=-Dlogback.configurationFile=/app/src/test/resources/logback.xml" \
    /jars/test-project-name-assembly-0.1.jar
```

To test that data was written to Elasticsearch, open up a browser and navigate to the following url: 
```bash
http://localhost:9200/usertestindex/_search
```
This should show all of the data from the original data.csv file written into the index "usertestindex" in Elasticsearch. Each row from the data is an individual doc entry in this case. 

## 5. Run Third Job
The third job reads from Elasticsearch's index that was created in the last job (testuserindex) and puts this data into a SparkSQL Dataframe. Afterwards, it displays the data in the console.
```bash
docker-compose exec dse1 dse spark-submit \
    --master dse://dse1 \
    --jars /jars/test-project-name-assembly-0.1.jar \
    --class "cassandraScalaEs.LoadFromEs" \
    --conf "spark.driver.extraJavaOptions=-Dlogback.configurationFile=/app/src/test/resources/logback.xml" \
    /jars/test-project-name-assembly-0.1.jar
```

This data is not filtered, but can be filtered with push-down operations (filter condition is automatically translated to a QueryDSL query which is then fed into elasticsearch by the elasticsearch spark connector, so that ES only gives back appropriate data)

See the following document for more information (Under Spark SQL Support section): 
https://www.elastic.co/guide/en/elasticsearch/hadoop/current/spark.html