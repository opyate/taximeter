package models

import spray.json.DefaultJsonProtocol
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._

case class Tariff(pence: Int, desc: String)

object TariffJsonProtocol extends DefaultJsonProtocol {
  implicit val TariffFormat = jsonFormat2(Tariff)
}