organization := "com.thoughtworks"

name := "idea-remote-pair"

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.1"

sbtVersion in ThisBuild := "0.13.6"

ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

//resolvers in ThisBuild ++= Seq(
//  "spray repo" at "http://repo.spray.io",
//  "ibiblio" at "http://mirrors.ibiblio.org/pub/mirrors/maven2",
//  "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
//  "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases",
//  "akka" at "http://repo.akka.io",
//  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
//)

libraryDependencies in ThisBuild ++= Seq(
  "net.liftweb" %% "lift-json" % "3.0-M2",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.slf4j" % "slf4j-api" % "1.7.7",
  "org.specs2" %% "specs2" % "2.4.2" % "test"
)

