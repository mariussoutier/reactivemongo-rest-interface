name := "rmongo-rest-interface"

version := "1.0"

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  cache,
  "org.reactivemongo" %% "reactivemongo" % "0.10.0-SNAPSHOT"
)

play.Project.playScalaSettings

scalariformSettings

org.scalastyle.sbt.ScalastylePlugin.Settings
