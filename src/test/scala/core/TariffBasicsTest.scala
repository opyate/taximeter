package core

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import models.Session
import models.Tick
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants


@RunWith(classOf[JUnitRunner])
class TariffBasicsTest extends FunSuite with Fixtures {
  
  test("Return the correct tariff by day and hour"){
    
    assert(Tariff1.getFor(DateTimeConstants.MONDAY, 0) == Tariff3, "Tariff3 for Monday from 00:00")
    
    jodaWeekdays.foreach(weekday => {
      assert(Tariff1.getFor(weekday, 0) == Tariff3, s"Tariff3 for $weekday from 00:00")
      assert(Tariff1.getFor(weekday, 6) == Tariff1, s"Tariff1 for $weekday from 6AM")
      assert(Tariff1.getFor(weekday, 20) == Tariff2, s"Tariff2 for $weekday from 8PM")
      assert(Tariff1.getFor(weekday, 22) == Tariff3, s"Tariff3 for $weekday from 10PM")
    })
    
    jodaWeekendDays.foreach(day => {
      assert(Tariff1.getFor(day, 0) == Tariff3, s"Tariff3 for day from 00:00")
      assert(Tariff1.getFor(day, 6) == Tariff2, s"Tariff1 for day from 6AM")
      assert(Tariff1.getFor(day, 22) == Tariff3, s"Tariff3 for day from 10PM")
    })
  }

}