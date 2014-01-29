package controllers

import scala.concurrent.Future
import scala.collection.JavaConverters._

import play.api._
import play.api.mvc._
import play.api.libs.ws.{ WS, Response }
import play.api.libs.ws.WS._
import play.api.libs.json._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Forwards a request to the MongoDB REST service
 */
class Forwarder extends MongoFwdDelegate with CurrentApp with CorsHeaders {

  /** Keep the original headers */
  def responseHeaders(response: Response): Seq[(String, String)] = {
    val headers = response.ahcResponse.getHeaders().keySet.asScala
    (for {
      headerName <- headers
      header <- response.header(headerName)
    } yield headerName -> header).toSeq
  }

  val forwardTo = Play.configuration
    .getString("mongoREST.simpleProxy.forwardTo")
    .getOrElse("http://localhost:28017")

  def find(path: String) = Action.async(BodyParsers.parse.empty) { request =>
    val forwardToCleansed =
      if (forwardTo.endsWith("/")) forwardTo.dropRight(1)
      else forwardTo
    WS.url(s"$forwardToCleansed/$path").get() map { response =>
      Results.Ok(response.body)
        .withHeaders(responseHeaders(response): _*)
        .withHeaders(corsHeaders: _*)
    }
  }

  def remove(path: String) = Action { _ =>
    Results.BadRequest(Json.obj("err" -> "MongoDB REST service does not support DELETE"))
  }
}
