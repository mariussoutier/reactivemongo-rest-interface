package controllers

import scala.collection.JavaConverters._
import scala.concurrent.Future

import play.api._
import play.api.mvc._
import play.api.http.ContentTypes
import play.api.Logger

import play.api.libs.json._
import play.api.libs.functional.syntax._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.BSONFormats.BSONDocumentFormat

/**
 * Connects directly to MongoDB to allow more than just GET requests.
 * Follows the same s
 */
class MongoRest extends MongoFwdDelegate with CurrentApp with CorsHeaders {

  val mongoUri: List[String] = Play.configuration
    .getStringList("mongoREST.mongodb.servers").map(_.asScala.toList)
    .getOrElse(List("mongodb://localhost:27017"))
  val driver = new MongoDriver
  val connection = driver.connection(mongoUri)

  // (<database name>, <collection name>)
  type Namespace = (String, String)

  def find(ignore: String) = Action.async { request =>
    val res: Future[List[JsObject]] = extractId(request.path) match {
      case Some(id) => findOne(id, extractNamespace(request.path), request.queryString)
      case None => findQuery(extractNamespace(request.path), request.queryString)
    }
    res map { res =>
      Results.Ok(Json.obj(
        "rows" -> res,
        "count" -> res.size,
        "total_rows" -> res.size // to stay compatible with MongoDB's REST interface
      // TODO offset, query, millis
      ))
    }
  }

  def remove(ignore: String) = Action.async { request =>
    extractId(request.path) match {
      case Some(id) => removeOne(id, extractNamespace(request.path)) map { lastError =>
        Results.Ok(Json.toJson(lastError)).as(ContentTypes.JSON)
      }
      case _ => badRequestAsync("Id is missing")
    }
  }

  def update(ignore: String) = Action.async(BodyParsers.parse.json) { request =>
    extractId(request.path) match {
      case Some(id) => updateOne(id, extractNamespace(request.path), request.body) map { lastError =>
        Results.Ok(Json.toJson(lastError)).as(ContentTypes.JSON)
      }
      case _ => badRequestAsync("Id is missing")
    }
  }

  def insert(ignore: String) = Action.async(BodyParsers.parse.json) { request =>
    insertOne(extractNamespace(request.path), request.body) map { lastError =>
      Results.Ok(Json.toJson(lastError)).as(ContentTypes.JSON)
    }
  }

  def badRequestAsync(message: String): Future[SimpleResult] =
    Future.successful(badRequest(message))

  def badRequest(message: String): SimpleResult =
    Results.BadRequest(Json.obj("err" -> message))

  implicit val lastErrorToJson: Writes[LastError] = (
    (__ \ "ok").write[Boolean] ~
    (__ \ "err").writeNullable[String] ~
    (__ \ "code").writeNullable[Int] ~
    (__ \ "errMsg").writeNullable[String] ~
    (__ \ "originalDocument").writeNullable[BSONDocument] ~
    (__ \ "updated").write[Int] ~
    (__ \ "updatedExisting").write[Boolean]
  )(unlift(LastError.unapply _))

  def trimPath(path: String): String = {
    val cleanedPath = if (path.head == '/') path.tail else path
    if (cleanedPath.last == '/') cleanedPath.init else cleanedPath
  }

  def extractNamespace(path: String): Namespace /*Try[Namespace]*/ = {
    trimPath(path).split("/").toList match {
      case dbName :: collection => (dbName, collection.head)
      case _ => throw new IllegalArgumentException("Required pattern is /dbName/collectionName")
    }
  }

  def extractId(path: String): Option[String] = {
    trimPath(path).split("/").toList match {
      case dbName :: collection :: id :: Nil => Some(id)
      case _ => None
    }
  }

  /**
   * Parse a String and try to guess the right Json value.
   * objectid() will be parsed to an ObjectId.
   */
  def parseValueToJson(string: String): JsValue = string match {
    case "true" => JsBoolean(true)
    case "false" => JsBoolean(false)
    case _ if string.toLowerCase.startsWith("objectid(") && string.endsWith(")") => {
      Json.obj("$oid" -> string.init.drop(9))
    }
    case _ => try {
      JsNumber(BigDecimal(string)) // TODO Make it possible to use String anyway, e.g. \s or ''
    } catch {
      case _: Throwable => JsString(string)
    }
  }

  /**
   * Parses all query parameter with key `query` to a JSON query object.
   * Divide key and value with a colon.
   */
  def parseQuery(queryString: Map[String, Seq[String]]): JsObject = {
    var res = Json.obj()
    for {
      values <- queryString.get("query")
      keyAndValue <- values
    } {
      val Array(key, value) = keyAndValue.split(":").take(2)
      res += key -> parseValueToJson(value)
    }
    if (queryString.nonEmpty)
      Logger.debug(s"Parsed $queryString into $res")
    res
  }

  def findQuery(ns: Namespace, queryString: Map[String, Seq[String]]): Future[List[JsObject]] = {
    Logger.debug(s"Find")
    val (dbName, collectionName) = ns
    val db = connection(dbName)
    val collection = db.collection[JSONCollection](collectionName)
    val query = parseQuery(queryString)
    collection.find(query).cursor[JsObject].collect[List]()
  }

  def findOne(id: String, ns: Namespace, queryString: Map[String, Seq[String]]): Future[List[JsObject]] = {
    Logger.debug(s"Find one $id")
    val (dbName, collectionName) = ns
    val db = connection(dbName)
    val collection = db.collection[JSONCollection](collectionName)
    val query = Json.obj("_id" -> parseValueToJson(id))
    collection.find(query).cursor[JsObject].collect[List]()
  }

  def removeOne(id: String, ns: Namespace): Future[LastError] = {
    Logger.debug(s"Removing $id")
    val (dbName, collectionName) = ns
    val db = connection(dbName)
    val collection = db.collection[JSONCollection](collectionName)
    val query = Json.obj("_id" -> parseValueToJson(id))
    collection.remove(query, firstMatchOnly = true)
  }

  def updateOne(id: String, ns: Namespace, data: JsValue): Future[LastError] = {
    Logger.debug(s"Updating $id")
    val (dbName, collectionName) = ns
    val db = connection(dbName)
    val collection = db.collection[JSONCollection](collectionName)
    collection.update(Json.obj("_id" -> parseValueToJson(id)), data)
  }

  def insertOne(ns: Namespace, data: JsValue): Future[LastError] = {
    Logger.debug(s"Inserting document")
    val (dbName, collectionName) = ns
    val db = connection(dbName)
    val collection = db.collection[JSONCollection](collectionName)
    collection.insert(data)
  }

}
