package api

import core.DefaultTimeout

import scala.util.{ Try }
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.model.headers.`Cache-Control`
import akka.http.scaladsl.model.headers.CacheDirectives.`no-cache`
import spray.json._

/**
 * @author alexandregenon
 */

class UI(implicit val actorSystem: ActorSystem) extends Directives with DefaultTimeout with TwirlSupport {
  import scala.concurrent.ExecutionContext.Implicits.global

  val stats = path("stats") {
    get {
      complete {
        html.mpl_stats()
      }
    }
  }

  // for webjar javascript dependencies
  val webjars = pathPrefix("webjars") {
    get {
      getFromResourceDirectory("META-INF/resources/webjars")
    }
  }

  val routes = respondWithHeaders(`Cache-Control` (`no-cache`)) {
    webjars ~ getFromResourceDirectory("assets") ~
      pathPrefix("ui") {
        stats ~
          pathEnd {
            get {
              complete {
                html.index()
              }
            }
          }
      }
  }
}