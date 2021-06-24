package cassandraScalaEs

import org.apache.log4j.{Level, LogManager, Logger}

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.cassandra._

import org.apache.spark.{SparkConf}

import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector

import org.elasticsearch.spark.sql._


object CassToEs {

  def main(args: Array[String]) {

    val log = LogManager.getLogger("cassandraScalaEs")
    log.setLevel(Level.INFO)
    log.info("************* START *************")

    //es.index.read.missing.as.empty set to yes allows for reading of non-existent 
    //indices without failure and just returns an empty data set.  
    val conf = new SparkConf()
      .setAppName("CassandraScalaEs")
      .set("spark.cassandra.connection.host", "localhost")
      .set("spark.cassandra.auth.username", "cassandra")
      .set("spark.cassandra.auth.password", "cassandra")
      .set("spark.driver.allowMultipleContexts", "true")
      .set("input.consistency.level", "LOCAL_ONE")
      .set("output.consistency.level", "LOCAL_ONE")
      .set("es.nodes", "es1")
      .set("es.port", "9200")
      .set("es.index.auto.create", "yes")
      .set("es.index.read.missing.as.empty", "yes")

    val spark = SparkSession.builder
      .config(conf)
      .getOrCreate

    //test keyspace and table name
    val ks = "test"
    val table1 = "users"
    
    val read_table = spark.read.cassandraFormat(table1, ks).load()

    //Filters can be read about more in depth here: https://github.com/datastax/spark-cassandra-connector/blob/v2.5.2/doc/14_data_frames.md
    //This filter should reduce load on the database. Example filter shows only columns where id > 1. 
    val read_table_with_filter = read_table.filter(read_table("id") > 1)
    read_table.show()
    read_table_with_filter.show()

    //Saves table to elasticsearch index usertestindex
    read_table.saveToEs("usertestindex")

    spark.stop()
  }
}
