package core

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import models.Session
import models.Tick
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants


@RunWith(classOf[JUnitRunner])
class TariffCalculatorCostByTimeTest extends FunSuite with Fixtures {

  test("Monday morning before 6AM has 100% ratio on Tariff3") {
  
    val monday3AM = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val monday3AMLater = monday3AM.plusMillis(35800)
    
    val previous = Session("test1", Tick(at = Some(monday3AM.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(monday3AMLater.getMillis()), odo = 0))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 240)
  }
  
  test("Monday morning before 6AM has 100% ratio on Tariff3, plus 17.9 seconds at another 20p") {
  
    val monday3AM = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val monday3AMLater = monday3AM.plusMillis(35800 + 17900)
    
    val previous = Session("test1", Tick(at = Some(monday3AM.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(monday3AMLater.getMillis()), odo = 0))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 260)
  }
  
  test("Monday morning before 6AM has 100% ratio on Tariff3, plus (114 * 17.9 seconds) to bring us to £25.20") {
  
    val monday3AM = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val monday3AMLater = monday3AM.plusMillis(35800 + (114 * 17900))
    
    val previous = Session("test1", Tick(at = Some(monday3AM.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(monday3AMLater.getMillis()), odo = 0))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 2520)
  }
  
  test("Monday morning before 6AM has 100% ratio on Tariff3, plus (114 * 17.9 seconds) + 19.2 to bring us to £25.40") {
  
    val monday3AM = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val monday3AMLater = monday3AM.plusMillis(35800 + (114 * 17900) + 19200)
    
    val previous = Session("test1", Tick(at = Some(monday3AM.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(monday3AMLater.getMillis()), odo = 0))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 2540)
  }
}