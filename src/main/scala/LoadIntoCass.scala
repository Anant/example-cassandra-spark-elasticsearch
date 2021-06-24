package cassandraScalaEs

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

import org.apache.spark.{SparkConf}

import org.apache.spark.sql.cassandra._
import com.datastax.spark.connector._



object LoadIntoCass {

  def main(args: Array[String]) {

    val conf = new SparkConf()
    .setAppName("CassandraScalaEs")
    .set("spark.cassandra.connection.host", "localhost")
    .set("spark.cassandra.auth.username", "cassandra")
    .set("spark.cassandra.auth.password", "cassandra")
    .set("spark.driver.allowMultipleContexts", "true")
    .set("input.consistency.level", "LOCAL_ONE")
    .set("output.consistency.level", "LOCAL_ONE")

    val spark = SparkSession
    .builder
    .config(conf)
    .getOrCreate

    //Location of csv file to read from
    val dataFile = "/app/test-data/data.csv"

    //Cassandra keyspace and table names
    val ks = "test"
    val table1 = "users"

    
    val csv_df = spark.read.format("csv").option("header", "true").load("file:///"+dataFile)
    //Show (in console) the dataframe made from opening the csv file
    csv_df.show()

    csv_df.write.cassandraFormat(table1, ks).mode("append").save()

    spark.stop()
  }

}