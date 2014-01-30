package models

import spray.json.DefaultJsonProtocol
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import org.joda.time.DateTime
import java.util.UUID

case class Session(id: String, at: Long, odo: Double, runningCost: Double, runningMillis: Long, runningMetres: Double)

object JsonProtocol extends DefaultJsonProtocol {
  implicit val SessionFormat = jsonFormat6(Session.apply)
}

case class Tick(at: Option[Long], odo: Double)

object Session {
  def apply(id: String, tick: Tick): Session = Session(id, tick.at.getOrElse(DateTime.now().getMillis), tick.odo, 240, 0, 0)
  def apply(uuid: UUID, tick: Tick): Session = Session(uuid.toString, tick)
  def apply(tick: Tick): Session = Session(UUID.randomUUID, tick)
}