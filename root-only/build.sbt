name := "sbt-tutorial"

version := "1.0"

scalaVersion := "2.11.8"

organization := "org.sbt-tutorial"

lazy val confFileName = settingKey[java.io.File]("Path to configuration settings")

confFileName := baseDirectory.value / "conf" / "gen_file.txt"


lazy val genConfFile = taskKey[Unit]("Creates a dummy configuration file")

genConfFile := {
  val time: String = new java.util.Date().toString
  println(time)
  IO.write(confFileName.value, s"time=$time")
}


cleanFiles := confFileName.value +: cleanFiles.value

