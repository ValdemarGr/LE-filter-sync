package filtersync.server 

import cats.effect._
import cats.implicits._
import fs2.concurrent.SignallingRef
import fs2.Stream
import cats.effect.std.Queue
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.HttpRoutes
import org.http4s.websocket.WebSocketFrame
import cats.effect.std.UUIDGen
import org.http4s.implicits._

trait ClientApi {
    def filterChanged(filter: String): IO[Unit]
}

trait ClientConnection {
    def resyncFilter(filter: String): IO[Unit]
}

final case class ClientConnectionState(
    conn: ClientConnection,
    newestFilter: Option[String]
)

final case class FilterState(
    clientFilters: Map[String, String],
    computedFilter: String
) {
    def recompute(f: Map[String, String] => Map[String, String]): FilterState = ???
}

class ServerState(connectedClients: SignallingRef[IO, FilterState]) {
    def connection(id: String): Resource[IO, ClientApi] = {
        Resource.make(IO.unit)(_ => connectedClients.update(_.recompute(_ - id))).as {
            new ClientApi {
                def filterChanged(filter: String): IO[Unit] = connectedClients.update(_.recompute(_ + (id -> filter)))
            }
        }
    }

    def server = {
        import org.http4s.dsl.io._
        def routes(wsb: WebSocketBuilder[IO]) = HttpRoutes.of[IO] {
            case GET -> Root / "ws" =>
                wsb.withFilterPingPongs(true).build(
                    connectedClients.discrete.map(_.computedFilter).map(WebSocketFrame.Text(_)),
                    stream => Stream.eval(UUIDGen.randomString[IO]).flatMap(id => Stream.resource(connection(id))).flatMap{ api =>
                        stream.evalMap{
                            case WebSocketFrame.Text(data, _) => api.filterChanged(data)
                            case _: WebSocketFrame.Close => IO.unit
                            case other => IO.raiseError(new RuntimeException(s"what is $other"))
                        }
                    }
                ) 
        }

                    import com.comcast.ip4s._
        implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
        EmberServerBuilder.default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpWebSocketApp(s => routes(s).orNotFound)
        .build
    }
}

object Main extends IOApp.Simple {
  override def run: IO[Unit] = {
    SignallingRef
        .of[IO, FilterState](FilterState(Map.empty, ""))
        .map(new ServerState(_))
        .flatMap(_.server.useForever)
  }
}