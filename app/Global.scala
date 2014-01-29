import play.api.{ Play, Configuration, Logger, Mode }

import controllers._

object Global extends play.api.GlobalSettings with CurrentApp {

  override def onLoadConfig(configuration: Configuration, path: java.io.File,
    classloader: ClassLoader, mode: Mode.Mode): Configuration = {
    if (configuration.getBoolean("mongoREST.simpleProxy.enabled").exists(_ == true)) {
      Logger.info("Simple proxy mode")
    } else {
      Logger.info("REST service mode")
    }
    configuration
  }

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    lookUpOrThrowUp(controllerClass)
  }

  /** Configure controller instance according to conf properties */
  def lookUpOrThrowUp[A](controllerClass: Class[A]): A = {
    if (controllerClass == classOf[MongoFwd]) {
      val delegate =
        if (Play.configuration.getBoolean("mongoREST.simpleProxy.enabled").exists(_ == true)) {
          new Forwarder
        } else {
          new MongoRest
        }
      new MongoFwd(delegate).asInstanceOf[A] // ah the dirty-ness
    } else throw new IllegalArgumentException
  }

}
