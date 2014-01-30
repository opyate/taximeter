package core

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import models.Session
import models.Tick
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants


@RunWith(classOf[JUnitRunner])
class TariffCalculatorCostByDistanceTest extends FunSuite with Fixtures {

  test("Monday morning before 6AM has 100% ratio on a single tariff") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = a.plusMillis(100)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 240) // but, BUT... I barely got into the cab!
  }
  
  test("Monday < 6AM went 165, which is JUST under the threshold") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = a.plusMillis(100)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 165))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 240) // Fair enough. At least we went around the corner this time.
  }
  
  test("Monday < 6AM went 167, which is over the threshold, but still not enough to trigger the extra charge") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = a.plusMillis(100)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 167))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 240) 
  }
  
  test("Monday < 6AM went 166.8  + 83.4, which is over the threshold, and the extra 83.4m will trigger the extra charge") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = a.plusMillis(100)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 166.8 + 83.4))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 260) // hey! 20p more than last time? There goes my coffee money.
  }
  /**
   * Tariff3
   * We go the 166.8 minimum, and another 114 * 83.4 meter stretches to reach the threshold of £25.20
   */
  test("Monday < 6AM went 166.8  + (114 * 83.4), which is over the threshold, and the extra 114 * 83.4m will bring us to 25.20") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = a.plusMillis(100)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 166.8 + (114 * 83.4)))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 2520) // Remortgage...
  }
  
    /**
   * Tariff3
   * We go the 166.8 minimum, and another 114 * 83.4 meter stretches to reach the threshold of £25.20
   * and then another 89.2 metres to bump it another 20p
   */
  test("Monday < 6AM went 166.8  + (114 * 83.4) + 89.2, which is over the threshold, and the extra 89.2 will bring us to 25.40") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = a.plusMillis(100)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 166.8 + (114 * 83.4) + 89.2))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 2540) // OK, I give up. Must. Walk.
  }

  /**
   * Tariff1 now...
   * We go the 254.6 minimum, and another 74 * 127.3 meter stretches to reach the threshold of £17.20
   */
  test("Monday > 6AM went 254.6  + (74 * 127.3), which is over the threshold, and the extra 74 * 127.3m will bring us to 17.20") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(9)
    val b = a.plusMillis(100)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 254.6 + (74 * 127.3) ))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 1720)
  }
  
  /**
   * Tariff1 now...
   * We go the 254.6 minimum, and another 74 * 127.3 meter stretches to reach the threshold of £17.20
   * and then another 89.2 metres to bump it another 20p
   */
  test("Monday > 6AM went 254.6  + (74 * 127.3) + 89.2, which is over the threshold, and the extra 89.2 will bring us to 17.40") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(9)
    val b = a.plusMillis(100)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 254.6 + (74 * 127.3) + 89.2))
    val newSession = Calculator.getCosts(previous, current)
    
    assert(newSession.runningCost == 1740)
  }
}