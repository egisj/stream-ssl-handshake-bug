name := "stream-ssl-handshake-bug"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= {
  val AkkaVersion = "2.5.19"
  
  Seq(
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
}
