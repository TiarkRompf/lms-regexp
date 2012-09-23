name := "lms-regexp"

version := "0.1"

organization := "EPFL"

resolvers += ScalaToolsSnapshots

//resolvers += dropboxScalaTestRepo

resolvers += prereleaseScalaTest

scalaOrganization := "org.scala-lang.virtualized"

//scalaBinaryVersion := virtScala // necessary??

scalaVersion := virtScala

scalacOptions += "-Yvirtualize"

//scalacOptions += "-Yvirtpatmat"

//scalacOptions in Compile ++= Seq(/*Unchecked, */Deprecation)

// needed for scala.tools, which is apparently not included in sbt's built in version
libraryDependencies += "org.scala-lang" % "scala-library" % virtScala

libraryDependencies += "org.scala-lang" % "scala-compiler" % virtScala

libraryDependencies += scalaTest

libraryDependencies += "EPFL" %% "lms" % "0.2"

// tests are not thread safe
parallelExecution in Test := false

// disable publishing of main docs
publishArtifact in (Compile, packageDoc) := false

// continuations
autoCompilerPlugins := true

addCompilerPlugin("org.scala-lang.plugins" % "continuations" % virtScala)

scalacOptions += "-P:continuations:enable"
