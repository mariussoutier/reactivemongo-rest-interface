package controllers

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.mvc._

/**
 * Connects directly to MongoDB to allow more than just GET requests.
 * Follows the same s
 */
class MongoRest extends MongoFwdDelegate with CurrentApp {

  val mongoUri = Play.configuration.getString("mongoREST.mongoUri").getOrElse("mongodb://localhost:27017")

  def forwardToMongo(path: String) = Action.async { request =>

    val responseFuture = request.method match {
      case "GET" =>
      //case "HEAD" => ws.head()
      //case "OPTIONS" => ws.options()
      case _ => Future.failed(new IllegalArgumentException("The MongoDB REST interface only supports GET requests"))
    }

    Future {
      Results.Ok
    }
  }

}
