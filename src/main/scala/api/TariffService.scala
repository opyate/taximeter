package api

import scala.concurrent.ExecutionContext
import spray.routing.Directives
import spray.http.MediaTypes.{ `application/json` }
import akka.actor.ActorRef
import akka.util.Timeout
import spray.httpx.SprayJsonSupport._
import com.redis.RedisClient
import spray.json._
import DefaultJsonProtocol._
import models.Tariff
import models.Tick
import models.Session
import models.JsonProtocol._
import core.TariffCalculator
import com.redis.serialization.DefaultFormats
import com.redis.serialization.Format
import spray.httpx.marshalling.Marshaller
import scala.concurrent.ExecutionContext.Implicits.global

class TariffService(calc: TariffCalculator) extends Directives {
  
  val route = {
    pathPrefix("api") {
      path("tariff" / JavaUUID) { uuid =>
        get {
          parameters(
            'at.as[Long] ?,
            'odo)
            .as(Tick) { tick =>
              respondWithMediaType(`application/json`) {
                complete {
                  calc.calc(Session(uuid, tick))
                }
              }
            }
        }
      }
    }
  }
}