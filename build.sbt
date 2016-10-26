name := """smartsox"""
version := "2.4.11"
scalaVersion := "2.11.8"

enablePlugins(JavaAppPackaging)

maintainer := "Nepomuk Seiler"
packageSummary := s"Akka ${version.value} Server"

libraryDependencies ++= Seq(
  "com.typesafe.akka"           %% "akka-actor" % version.value,
  "com.typesafe.akka"           %% "akka-cluster" % version.value,
  "com.typesafe.akka"           %% "akka-cluster-tools" % version.value,
  "com.typesafe.akka"           %% "akka-persistence" % version.value,
  "com.typesafe.akka"           %% "akka-slf4j" % version.value,
  "com.typesafe.akka"           %% "akka-testkit" % version.value % "test",
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
  "ch.qos.logback"              % "logback-classic" % "1.1.7",
  "org.scalatest"               %% "scalatest" % "2.2.1" % "test",
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

