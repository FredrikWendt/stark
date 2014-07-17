package se.marcuslonnberg.stark

import akka.actor.ActorSystem
import akka.event.Logging
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import se.marcuslonnberg.stark.api.ApiActor
import se.marcuslonnberg.stark.proxy.ProxiesActor
import spray.can.Http
import spray.can.server.ServerSettings
import spray.http.Uri

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object StarkProxyApp extends App with SSLSupport {
  implicit val system = ActorSystem("stark")
  import se.marcuslonnberg.stark.StarkProxyApp.system.dispatcher
  sys.addShutdownHook(system.shutdown())

  val log = Logging(system, this.getClass)
  val conf = ConfigFactory.load()

  val proxies = system.actorOf(ProxiesActor.props(), "proxies")
  val apiRouting = system.actorOf(ApiActor.props(proxies), "api-routing")

  val apiHost = Uri.Host(conf.as[String]("server.api-host"))
  val connector = system.actorOf(ConnectActor.props(proxies, apiRouting, apiHost), "connector")

  val bindings = conf.as[List[Config]]("server.bindings")
  log.info(s"Found ${bindings.length} bindings in config")
  implicit val timeout = Timeout(30.seconds)
  bindings.foreach(bind)

  def bind(bindConfig: Config) = {
    val bindInterface = bindConfig.as[Option[String]]("interface").getOrElse(conf.as[String]("server.default-interface"))
    val port = bindConfig.as[Int]("port")

    case class SLLConfig(`cert-file`: String, `private-key-file`: String)
    import net.ceedubs.ficus.readers.ArbitraryTypeReader._
    val ssl = bindConfig.as[Option[SLLConfig]]("ssl")

    val bind = {
      val bind = Http.Bind(connector, interface = bindInterface, port = port)
      ssl match {
        case Some(SLLConfig(certPath, privateKeyPath)) =>
          implicit val sslContext = createSSLContext(certPath, privateKeyPath)
          bind.copy(settings = Some(ServerSettings(conf).copy(sslEncryption = true)))
        case None =>
          bind
      }
    }

    (IO(Http) ? bind).onComplete {
      case Success(Http.Bound(address)) =>
        log.info("Bound to {}", address)
      case Success(Http.CommandFailed(cmd)) =>
        log.error("Could not bind to {}, reason: {}", bind.endpoint, cmd.failureMessage)
      case Success(other) =>
        log.error("Could not bind to {}, unknown response: {}", bind.endpoint, other)
      case Failure(ex) =>
        log.error(ex, "Could not bind to {}", bind.endpoint)
    }
  }
}
