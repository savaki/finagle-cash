name := "finagle-cash"

organization := "com.github.savaki"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.1"

resolvers += "Twitter Repo" at "http://maven.twttr.com"

{
    val finagleVersion = "6.2.1"
    libraryDependencies ++= Seq(
        "com.twitter" %% "finagle-core" % finagleVersion withSources(),
        "com.twitter" %% "finagle-native" % finagleVersion withSources(),
        "com.twitter" %% "finagle-redis" % finagleVersion withSources(),
        "com.twitter" %% "finagle-memcached" % finagleVersion withSources(),
        "com.twitter" %% "finagle-serversets" % finagleVersion withSources(),
        "com.twitter" %% "finagle-http" % finagleVersion withSources()
    )
}

{
    libraryDependencies ++= Seq(
        "org.scalatest" % "scalatest_2.10" % "1.9.1"
    )
}

