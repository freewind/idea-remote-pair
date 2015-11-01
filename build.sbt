organization := "com.thoughtworks"


name := "idea-remote-pair"

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.7"

sbtVersion in ThisBuild := "0.13.9"

ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

resolvers in ThisBuild ++= Seq(
  "Scalaz" at "http://dl.bintray.com/scalaz/releases",
  "spray repo" at "http://repo.spray.io",
  "ibiblio" at "http://mirrors.ibiblio.org/pub/mirrors/maven2",
  "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases",
  "akka" at "http://repo.akka.io",
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
)

libraryDependencies in ThisBuild ++= Seq(
U  "com.thoughtworks" %% "remote-pair-server" % "0.6.0",
  "commons-lang" % "commons-lang" % "2.6",
  "commons-io" % "commons-io" % "2.0.1",
  "org.scalaz" %% "scalaz-core" % "7.1.3",
  "org.scalaz" %% "scalaz-effect" % "7.1.3",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.json4s" %% "json4s-core" % "3.2.11",
  "org.json4s" %% "json4s-ext" % "3.2.11",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.slf4j" % "slf4j-api" % "1.7.7",
  "org.specs2" %% "specs2-mock" % "2.4.2" % "test",
  "org.specs2" %% "specs2" % "2.4.2" % "test",
  "org.apache.commons" % "commons-vfs2" % "2.0" % "test",
  "io.netty" % "netty-all" % "5.0.0.Alpha1"
)

//retrieveManaged := true

lazy val convertToPluginProject = taskKey[Unit]("Convert current project to plugin project")

convertToPluginProject := {
  PluginConverter.convertToPlugin(baseDirectory.value / ".idea" / "modules" / (name.value + ".iml"))
  PluginConverter.createPluginTask(baseDirectory.value / ".idea" / "workspace.xml", name.value)
}

