import sbt.Keys.libraryDependencies

import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.0"
scalacOptions += "-Ypartial-unification"
lazy val root = (project in file("."))
  .settings(
    name := "Scala-QLearning",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0"
    )
  )
