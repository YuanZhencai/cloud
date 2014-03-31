import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "CloudWMS"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
     "be.objectify" %% "deadbolt-java" % "2.1-RC2",
 	 "be.objectify" %% "deadbolt-scala" % "2.1-RC2",
 	 "org.apache.poi" % "poi" % "3.8",
 	 "org.apache.poi" % "poi-ooxml" % "3.8",
 	 "com.typesafe" %% "play-plugins-mailer" % "2.1.0"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
  resolvers += Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns)
    // Add your own project settings here      
  )

}
