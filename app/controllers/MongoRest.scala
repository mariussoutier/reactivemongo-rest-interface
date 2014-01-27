package controllers

import scala.collection.JavaConverters._
import scala.concurrent.Future

import play.api._
import play.api.mvc._
import play.api.http.ContentTypes
import play.api.libs.json._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import reactivemongo.api._
import reactivemongo.bson._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection

/**
 * Connects directly to MongoDB to allow more than just GET requests.
 * Follows the same s
 */
class MongoRest extends MongoFwdDelegate with CurrentApp /*with MongoController*/ {

  val mongoUri: List[String] = Play.configuration
    .getStringList("mongoREST.mongodb.servers").map(_.asScala.toList)
    .getOrElse(List[String]("mongodb://localhost:27017"))
  val driver = new MongoDriver
  val connection = driver.connection(mongoUri)

  // (Database name, collection name)
  type Namespace = (String, String)

  def forwardToMongo(ignore: String) = Action.async { request =>

    val responseFuture: Future[List[JsObject]] = request.method match {
      case "GET" => find(extractNamespace(request.path), request.queryString)
      case _ => Future.failed(new IllegalArgumentException("The MongoDB REST interface only supports GET requests"))
    }

    responseFuture map { res =>
      Results.Ok(
        Json.obj(
          "rows" -> res,
          "count" -> res.size
        )
      ).as(ContentTypes.JSON)
    }
  }

  def extractNamespace(path: String): Namespace /*Try[Namespace]*/ = {
    val cleanedPath = if (path(0) == '/') path.drop(1) else path
    cleanedPath.split("/").toList match {
      case dbName :: collection =>
        println("!!!" + dbName); (dbName, collection.mkString("."))
      case _ => throw new IllegalArgumentException("Required pattern is /dbName/collectionName")
    }
  }

  def find(ns: Namespace, queryString: Map[String, Seq[String]]): Future[List[JsObject]] = {
    val (dbName, collectionName) = ns
    val db = connection(dbName)
    val collection = db.collection[JSONCollection](collectionName)
    // parse
    collection.find(Json.obj()).cursor[JsObject].collect[List]()
  }

}
