import akka.actor.ActorSystem
import akka.stream.TLSProtocol.NegotiateNewSession
import akka.stream.scaladsl.{BidiFlow, Flow, Sink, Source, TLS, Tcp}
import akka.stream._
import akka.util.ByteString
import javax.net.ssl.SSLContext

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Application {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()

    val host = "google.com"
    val port = 443

    val done = Source.empty[ByteString]
      .via(outgoingTlsConnection(host, port, SSLContext.getDefault, TLSProtocol.NegotiateNewSession.withDefaults))
      .runWith(Sink.ignore)

    Await.result(done, 5.seconds)
  }

  // This is copy pasta from akka.stream.scaladsl.Tcp, except TLSClosing is set to EagerClose.
  def outgoingTlsConnection(
    host: String,
    port: Int,
    sslContext: SSLContext,
    negotiateNewSession:
    NegotiateNewSession)(implicit system: ActorSystem): Flow[ByteString, ByteString, Future[Tcp.OutgoingConnection]] = {

    val connection = Tcp().outgoingConnection(host, port)
    val tlsWrapping = BidiFlow.fromFlows(
      Flow[ByteString].map(TLSProtocol.SendBytes),
      Flow[TLSProtocol.SslTlsInbound].collect {
        case sb: TLSProtocol.SessionBytes ⇒ sb.bytes
        // ignore other kinds of inbounds (currently only Truncated)
      }
    )

    val tls = TLS(sslContext, negotiateNewSession, TLSRole.client, EagerClose, None)
    connection.join(tlsWrapping.atop(tls).reversed)
  }
}