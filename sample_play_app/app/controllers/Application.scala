package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

object Application extends Controller {

  def showCache(key: String) = Action {
    Ok(Cache.getAs[String](key).getOrElse("Nothing found"))
  }
}