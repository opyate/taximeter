import com.typesafe.sbt.SbtStartScript

import SbtStartScript.StartScriptKeys._

name := "taximeter"

version := "0.1.0"

scalaVersion := "2.10.3"

seq(SbtStartScript.startScriptForClassesSettings: _*)

seq(Revolver.settings: _*)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "-feature"
)

parallelExecution in Test := false

resolvers ++= Seq(
  "spray" at "http://repo.spray.io/",
  "Spray Nightlies" at "http://nightlies.spray.io/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.2.3",
  "io.spray" % "spray-can" % "1.2-20130712",
  "io.spray" % "spray-client" % "1.2-20130712",
  "io.spray" % "spray-routing" % "1.2-20130712",
  "io.spray" %% "spray-json" % "1.2.5",
  "net.debasishg" %% "redisreact" % "0.3",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.5",
  "org.specs2" %% "specs2" % "2.2.2" % "test",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "junit" % "junit" % "4.11" % "test",
  "io.spray" % "spray-testkit" % "1.2.0" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3" % "test"
)
