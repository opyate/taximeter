package core

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import models.Session
import models.Tick
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants


@RunWith(classOf[JUnitRunner])
class TariffCalculatorTest extends FunSuite with Fixtures {

  test("Monday morning before 6AM has Tariff3") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = a.plusMillis(100)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
    val intervals = Calculator.getTariffIntervals(previous, current)
    
    assert(intervals.head.fromTariff == Tariff3)
    assert(intervals.last.fromTariff == Tariff3)
    assert(intervals.size == 1)
  }
  
  test("Monday between 6AM and 8PM has Tariff1") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(10)
    val b = a.plusMillis(100)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
    val intervals = Calculator.getTariffIntervals(previous, current)
    
    assert(intervals.head.fromTariff == Tariff1)
    assert(intervals.last.fromTariff == Tariff1)
    assert(intervals.size == 1)
  }
  
  test("Weekday morning before 6AM has Tariff3") {

    jodaWeekdays.map(now.withDayOfWeek(_)).foreach(day => {
      val a = day.withHourOfDay(3)
      val b = a.plusMillis(100)

      val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
      val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
      val intervals = Calculator.getTariffIntervals(previous, current)

      assert(intervals.head.fromTariff == Tariff3)
      assert(intervals.last.fromTariff == Tariff3)
      assert(intervals.size == 1)
    })
  }
  
  test("Weekday between 6AM and 8PM has Tariff1") {

    jodaWeekdays.map(now.withDayOfWeek(_)).foreach(day => {
      val a = day.withHourOfDay(10)
      val b = a.plusMillis(100)

      val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
      val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
      val intervals = Calculator.getTariffIntervals(previous, current)

      assert(intervals.head.fromTariff == Tariff1)
      assert(intervals.last.fromTariff == Tariff1)
      assert(intervals.size == 1)
    })
  }
  
  test("Weekday between 8PM and 10PM has Tariff2") {

    jodaWeekdays.map(now.withDayOfWeek(_)).foreach(day => {
      val a = day.withHourOfDay(21)
      val b = a.plusMillis(100)

      val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
      val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
      val intervals = Calculator.getTariffIntervals(previous, current)

      assert(intervals.head.fromTariff == Tariff2)
      assert(intervals.last.fromTariff == Tariff2)
      assert(intervals.size == 1)
    })
  }

  test("Weekday after 10PM has Tariff3") {

    jodaWeekdays.map(now.withDayOfWeek(_)).foreach(day => {
      val a = day.withHourOfDay(23)
      val b = a.plusMillis(100)

      val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
      val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
      val intervals = Calculator.getTariffIntervals(previous, current)

      assert(intervals.head.fromTariff == Tariff3)
      assert(intervals.last.fromTariff == Tariff3)
      assert(intervals.size == 1)
    })
  }
  
  test("Weekend morning before 6AM has Tariff3") {

    jodaWeekendDays.map(now.withDayOfWeek(_)).foreach(day => {
      val a = day.withHourOfDay(3)
      val b = a.plusMillis(100)

      val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
      val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
      val intervals = Calculator.getTariffIntervals(previous, current)

      assert(intervals.head.fromTariff == Tariff3)
      assert(intervals.last.fromTariff == Tariff3)
      assert(intervals.size == 1)
    })
  }
  
  test("Weekend between 6AM and 10PM has Tariff2") {

    jodaWeekendDays.map(now.withDayOfWeek(_)).foreach(day => {
      val a = day.withHourOfDay(10)
      val b = a.plusMillis(100)

      val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
      val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
      val intervals = Calculator.getTariffIntervals(previous, current)

      assert(intervals.head.fromTariff == Tariff2)
      assert(intervals.last.fromTariff == Tariff2)
      assert(intervals.size == 1)
    })
  }
  
  test("Weekend after 10PM has Tariff3") {

    jodaWeekendDays.map(now.withDayOfWeek(_)).foreach(day => {
      val a = day.withHourOfDay(23)
      val b = a.plusMillis(100)

      val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
      val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
      val intervals = Calculator.getTariffIntervals(previous, current)

      assert(intervals.head.fromTariff == Tariff3)
      assert(intervals.last.fromTariff == Tariff3)
      assert(intervals.size == 1)
    })
  }
}