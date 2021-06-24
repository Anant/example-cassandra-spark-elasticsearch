package cassandraScalaEs

import org.apache.log4j.{Level, LogManager, Logger}

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

import org.apache.spark.{SparkConf}

import org.elasticsearch.spark.sql._


object LoadFromEs {

  def main(args: Array[String]) {

    //case class User(id:String, username:String)

    val log = LogManager.getLogger("cassandraScalaEs")
    log.setLevel(Level.INFO)
    log.info("************* START *************")

    //es.index.read.missing.as.empty set to yes allows for reading of non-existent 
    //indices without failure and just returns an empty data set.  
    val conf = new SparkConf()
      .setAppName("CassandraScalaEs")
      .set("es.nodes", "es1")
      .set("es.port", "9200")
      .set("es.index.auto.create", "yes")
      .set("es.index.read.missing.as.empty", "yes")

    val spark = SparkSession.builder
      .config(conf)
      .getOrCreate

    //test index name in ES
    val testIndex = "usertestindex"


    val df = spark.read.format("es").load("usertestindex")
    df.show()

    //At this point, .filter can be used to filter this information from ES.

    spark.stop()
  }
}
