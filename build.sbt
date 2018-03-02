name := "processing-scala-sample"

version := "1.0"

scalaVersion in ThisBuild := "2.12.2"

classpathTypes ++= Set("jnilib")

resolvers += MavenRepository("jogamp", "http://jogamp.org/deployment/maven")

libraryDependencies += "org.typelevel" %% "spire" % "0.14.1"

libraryDependencies += "org.jogamp.jogl" % "jogl-all" % "2.3.2"

libraryDependencies += "org.jogamp.jogl" % "newt" % "2.3.2"

libraryDependencies += "org.jogamp.gluegen" % "gluegen-rt" % "2.3.2"

fork in run := true

