package controllers

import play.api._
import play.api.mvc._

/** The delegate is needed for DI - the controller class is statically defined in the routes files */
trait MongoFwdDelegate {
  //def forwardToMongo(path: String): Action[_]

  def find(path: String): Action[_]
  def remove(path: String): Action[_]
  /*def update(path: String): Action[_]
  def insert(path: String): Action[_]*/
}

/** Delegate is injected via controller DI in the Global object */
class MongoFwd(delegate: MongoFwdDelegate) extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  /** Forward the current request to the actual service */
  //def forwardToMongo(path: String) = delegate.forwardToMongo(path)

  def find(path: String) = delegate.find(path)
  def remove(path: String) = delegate.remove(path)

}

/** Make this implicit dependency explicit - makes it a whole lot easier to test */
trait CurrentApp {
  implicit lazy val app = play.api.Play.current
}
