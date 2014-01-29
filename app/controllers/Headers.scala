package controllers

import play.api._

trait CorsHeaders { self: CurrentApp =>

  val corsHeaders: Seq[(String, String)] = {
    var headers = collection.mutable.ArrayBuffer[(String, String)]()
    if (Play.configuration.getBoolean("mongoREST.cors.enabled").exists(_ == true)) {
      val allowedOrigin = Play.configuration.getString("mongoREST.cors.origin")
      headers += "Access-Control-Allow-Origin" -> allowedOrigin.getOrElse("*")
    }
    headers.toSeq
  }

}
