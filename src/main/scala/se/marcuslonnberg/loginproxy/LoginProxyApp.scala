package se.marcuslonnberg.loginproxy

import akka.actor.ActorSystem
import akka.io.IO
import spray.can.Http
import spray.http.Uri
import se.marcuslonnberg.loginproxy.proxy.{ProxyActor, Host, ProxyConf}
import se.marcuslonnberg.loginproxy.auth.AuthActor
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._
import se.marcuslonnberg.loginproxy.api.ProxyApiActor.AddProxy
import scala.concurrent.duration._

object LoginProxyApp extends App {
  implicit val system = ActorSystem("proxy")

  val conf = ConfigFactory.load()

  val apiDomain = Uri.Host(conf.as[String]("server.apiDomain"))
  val proxy = system.actorOf(ProxyActor.props(apiDomain), "proxy")
  val auth = system.actorOf(AuthActor.props(), "auth")

  val connector = system.actorOf(ConnectActor.props(proxy, auth), "connector")

  import system.dispatcher
  system.scheduler.scheduleOnce(1.seconds) {
    val proxyApi = system.actorSelection(proxy.path / "api-routing" / "api")
    proxyApi ! AddProxy(ProxyConf(Host("bbc.local"), Uri("http://www.bbc.co.uk")))
    proxyApi ! AddProxy(ProxyConf(Host("cnn.local"), Uri("http://edition.cnn.com")))
  }

  IO(Http) ! Http.Bind(connector, interface = "localhost", port = 8081)
}
