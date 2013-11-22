package controllers

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import play.api.libs.ws.WS._

/** Forwards a request to the MongoDB REST service */
class Forwarder extends MongoFwdDelegate with CurrentApp {

  val extraHeaders: Seq[(String, String)] = {
    var headers = collection.mutable.ArrayBuffer[(String, String)]()
    if (Play.configuration.getBoolean("mongoREST.cors.enabled").exists(_ == true))
      headers += "Access-Control-Allow-Origin" -> "*"

    // TODO x-ns: db.Coll
    headers.toSeq
  }

  val forwardTo = Play.configuration.getString("mongoREST.simpleProxy.forwardTo").getOrElse("http://localhost:28017")

  def forwardToMongo(path: String) = Action.async(BodyParsers.parse.empty) { request =>
    val forwardToCleansed =
      if (forwardTo.endsWith("/")) forwardTo.dropRight(1)
      else forwardTo
    WS.url(s"$forwardToCleansed/$path").get() map { response =>
      Results.Ok(response.body).withHeaders(extraHeaders: _*)
    }
  }
}
