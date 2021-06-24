ThisBuild / organization := "com.anant"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.11.12"


val esVersion = "7.13.0"
val sparkVersion = "2.2.3"

lazy val root = (project in file("."))
  .settings(
    name := "test-project-name",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
      "com.datastax.dse" % "dse-spark-dependencies" % "6.7.7" % "provided", 
      "org.elasticsearch" % "elasticsearch-spark-20_2.11" % esVersion
    ),
    resolvers ++= Seq(
      "DataStax-Repo" at "https://repo.datastax.com/public-repos/"
    )
  )