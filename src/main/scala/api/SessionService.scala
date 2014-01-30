package api

import scala.concurrent.ExecutionContext
import spray.routing.Directives

import akka.util.Timeout
import spray.httpx.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._
import models.Tick
import spray.http.MediaTypes.{ `application/json` }
import models.JsonProtocol._
import core.Cache

class SessionService(cache: Cache) extends Directives {
  
  val route = {
    pathPrefix("api") {
      path("session") {
        get {
          parameters(
            'at.as[Long] ?,
            'odo
            )
            .as(Tick) { firstTick =>
              respondWithMediaType(`application/json`) {
                complete {
                  cache.newSession(firstTick)
                }
              }
            }
        }
      }
    }
  }
}