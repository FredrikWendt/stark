import sbt._

object Dependencies {
  val Akka = {
    val version = "2.3.9"
    Seq("com.typesafe.akka" %% "akka-actor" % version,
      "com.typesafe.akka" %% "akka-slf4j" % version)
  }

  val Spray = {
    val version = "1.3.2"
    Seq("io.spray" %% "spray-can" % version,
      "io.spray" %% "spray-client" % version,
      "io.spray" %% "spray-routing" % version,
      "io.spray" %% "spray-testkit" % version)
  }

  val Json4s = {
    val version = "3.2.11"
    Seq("org.json4s" %% "json4s-native" % version,
      "org.json4s" %% "json4s-ext" % version)
  }

  val ScalaTest = Seq("org.scalatest" %% "scalatest" % "2.2.4" % "test")

  val Ficus = Seq("net.ceedubs" %% "ficus" % "1.1.2")

  val Logback = Seq("ch.qos.logback" % "logback-classic" % "1.1.2")

  val NScala = Seq("com.github.nscala-time" %% "nscala-time" % "1.8.0")

  val CommonsCodec = Seq("commons-codec" % "commons-codec" % "1.10")

  val RedisScala = Seq("com.etaty.rediscala" %% "rediscala" % "1.4.2")

  val NotYetCommonsSSL = Seq("ca.juliusdavies" % "not-yet-commons-ssl" % "0.3.11")

  val all = Akka ++ Spray ++ Json4s ++ ScalaTest ++ Ficus ++ Logback ++ NScala ++ CommonsCodec ++ RedisScala ++ NotYetCommonsSSL

  object Resolvers {
    val Spray = "spray repo" at "http://repo.spray.io/"

    val RedisScala = "rediscala" at "http://dl.bintray.com/etaty/maven"

    val all = Spray :: RedisScala :: Nil
  }

}
