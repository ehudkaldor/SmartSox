name := """smartsox"""
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.14"

enablePlugins(JavaAppPackaging, SbtWeb)


maintainer := "Ehud Kaldor"
packageSummary := s"Akka ${version.value} Server"

resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(

  //nscala-time
  "com.github.nscala-time" %% "nscala-time" % "2.16.0",

  //akka
  "com.typesafe.akka"           %% "akka-actor" % akkaVersion,
  "com.typesafe.akka"           %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka"           %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka"           %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka"           %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka"           %% "akka-testkit" % akkaVersion % "test",
  
  //akka-http
  "com.typesafe.akka"           %% "akka-http" % "10.0.0" ,
  
  //akka db (persistence
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
  "com.github.dnvriend"         %% "akka-persistence-inmemory" % "1.3.11",
  
  //json4s
  "org.json4s"                  %% "json4s-native" % "3.5.0",
  "org.json4s"                  %% "json4s-ext" % "3.5.0",
  "de.heikoseeberger"           %% "akka-http-json4s" % "1.11.0",
  
  "ch.qos.logback"              % "logback-classic" % "1.1.7",
  "org.scalatest"               %% "scalatest" % "2.2.1" % "test",
  "com.github.romix.akka"       %% "akka-kryo-serialization" % "0.4.1",
  "com.github.scopt"            %% "scopt" % "3.2.0"
)


// Create custom run tasks to start a seed and a cluster node
// http://www.scala-sbt.org/0.13.0/docs/faq.html#how-can-i-create-a-custom-run-task-in-addition-to-run
lazy val runSeed = taskKey[Unit]("Start the seed node on 127.0.0.1:2551")
fullRunTask(runSeed, Compile, "com.example.Main", "--seed")
fork in runSeed := true

javaOptions in runSeed ++= Seq(
    "-Dclustering.ip=127.0.0.1",
    "-Dclustering.port=2551"
)

lazy val runNode = taskKey[Unit]("Start a node on 127.0.0.1:2552")
fullRunTask(runNode, Compile, "com.example.Main", "127.0.0.1:2551")
fork in runNode := true

javaOptions in runNode ++= Seq(
    "-Dclustering.ip=127.0.0.1",
    "-Dclustering.port=2552"
)

