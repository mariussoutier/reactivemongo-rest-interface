package controllers

import play.api._
import play.api.mvc._

trait MongoFwdDelegate {
  def forwardToMongo(path: String): Action[_]
}

class MongoFwd(delegate: MongoFwdDelegate) extends Controller {
  def index = Action {
    Ok(views.html.index())
  }

  def forwardToMongo(path: String) = delegate.forwardToMongo(path)

}

//object MongoFwd extends MongoFwd

/** Make this implicit dependency explicit - makes it a whole lot easier to test */
trait CurrentApp {
  implicit lazy val app = play.api.Play.current
}
