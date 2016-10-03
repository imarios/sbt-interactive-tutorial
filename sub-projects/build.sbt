
lazy val common = Seq( 
   version := "1.0",
   scalaVersion := "2.11.8",
   organization := "org.sbt-tutorial"
  )

lazy val confFileName = settingKey[java.io.File]("Path to configuration settings")
lazy val genConfFile = taskKey[Unit]("Creates a dummy configuration file")

lazy val root = project.in(file(".")).settings(
   common,
   name := "sbt-withsubprojects",
   confFileName := baseDirectory.value / "conf" / "gen_file.txt",
   genConfFile := {
      val time: String = new java.util.Date().toString
      println(time)
      IO.write(confFileName.value, s"time=$time")
   },
   cleanFiles := confFileName.value +: cleanFiles.value
).aggregate(alpha,beta)


lazy val alpha = project.in(file("sub-a")).
   settings(
      common,
      name := "alpha" 
   )

lazy val beta = project.in(file("sub-b")).
   settings(
      common,
      name := "beta"
   )   
