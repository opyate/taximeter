package core

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import models.Session
import models.Tick
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants


@RunWith(classOf[JUnitRunner])
class TariffCalculatorLongTimeJourneyTest extends FunSuite with Fixtures {

  test("Long journey on Monday starting before 6AM and ending after 8AM crosses a tariff boundary") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = a.plusHours(6)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
    val intervals = Calculator.getTariffIntervals(previous, current)
    
    assert(intervals.head.fromTariff == Tariff3)
    assert(intervals.last.fromTariff == Tariff1)
    assert(intervals.size == 2)
  }
  
  test("Long journey on Monday starting before 6AM and ending after 8PM crosses 2 tariff boundaries") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = a.plusHours(18)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
    val intervals = Calculator.getTariffIntervals(previous, current)
    
    assert(intervals.head.fromTariff == Tariff3)
    assert(intervals.last.fromTariff == Tariff2)
    assert(intervals.size == 3)
  }
  
  test("Long journey on Monday starting before 6AM and ending after 10PM crosses 3 tariff boundaries") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = a.plusHours(20)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
    val intervals = Calculator.getTariffIntervals(previous, current)
    
    assert(intervals.head.fromTariff == Tariff3)
    assert(intervals.last.fromTariff == Tariff3)
    assert(intervals.size == 4)
  }
  
  /**val intervals
   * We cross the following tariff boundaries:
   * 
   * 1. 6AM on Monday
   * 2. 8PM
   * 3. 10PM
   * 4. midnight (even though the tariff before and after this point could be the same. Reminder: to ease public holiday handling)
   * 5. 6AM
   * 6. 8PM
   * 7. 10PM on Tuesday
   */
  test("Long journey on Monday starting before 6AM and ending Tuesday after 10PM crosses 3 tariff boundaries") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = now.withDayOfWeek(DateTimeConstants.TUESDAY).withHourOfDay(23)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
    val intervals = Calculator.getTariffIntervals(previous, current)
    
    assert(intervals.head.fromTariff == Tariff3)
    assert(intervals.last.fromTariff == Tariff3)
    assert(intervals.size == 8)
  }
}