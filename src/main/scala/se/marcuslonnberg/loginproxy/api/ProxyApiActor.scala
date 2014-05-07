package se.marcuslonnberg.loginproxy.api

import akka.actor.{ActorLogging, Actor, Props, ActorRef}
import se.marcuslonnberg.loginproxy.proxy.{Host, ProxyConf}
import se.marcuslonnberg.loginproxy.proxy.ProxyActor.SetProxies
import se.marcuslonnberg.loginproxy.api.ProxyApiActor._

object ProxyApiActor {
  def props(proxyActor: ActorRef) = Props(classOf[ProxyApiActor], proxyActor)

  case class AddProxy(proxy: ProxyConf)

  case object GetProxies

  case class GetProxy(host: Host)


  object Responses {
    case class AddProxy(proxy: ProxyConf)

    case class GetProxies(proxies: List[ProxyConf])

    case class GetProxy(proxy: Option[ProxyConf])

  }

}

class ProxyApiActor(proxyActor: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = state(List.empty)

  def state(proxies: List[ProxyConf]): Receive = {
    case GetProxies =>
      sender ! Responses.GetProxies(proxies)
    case AddProxy(proxy) =>
      log.info("Adding proxy {}", proxy)
      val newProxies = proxy +: proxies
      proxyActor ! SetProxies(newProxies)
      context.become(state(newProxies))
      sender ! Responses.AddProxy(proxy)
    case GetProxy(host) =>
      sender ! Responses.GetProxy(proxies.find(_.host == host))
  }
}