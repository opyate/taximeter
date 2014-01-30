package core

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import models.Session
import models.Tick
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

case class Precision(val p:Double)

object DoublePimp {
  implicit class DoubleWithAlmostEquals(val d:Double) extends AnyVal {
    def ~=(d2:Double)(implicit p:Precision) = (d - d2).abs < p.p
  }
}


@RunWith(classOf[JUnitRunner])
class TariffCalculatorRatiosTest extends FunSuite with Fixtures {
  
  import DoublePimp._
  implicit val precision = Precision(0.001)

  test("Monday morning before 6AM has 100% ratio on a single tariff") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(3)
    val b = a.plusMillis(100)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
    val ratios = Calculator.getRatios(previous, current)
    
    assert(ratios.size == 1)
    assert(ratios.head.ratio == 1.0)
    assert(ratios.head.tariff == Tariff3)
  }

  test("Monday morning an hour before 6AM and 2 hours after 6AM has 33% ratio on Tariff3 and 66% ratio on Tariff1") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(5)
    val b = a.plusHours(3)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
    val ratios = Calculator.getRatios(previous, current)
    
    assert(ratios.size == 2)
    assert(ratios.head.ratio ~= 0.333)
    assert(ratios.head.tariff == Tariff3)
    assert(ratios.last.ratio ~= 0.666)
    assert(ratios.last.tariff == Tariff1)
  }
  
  test("Monday morning an hour before 6AM and 1 hour after 8PM has these ratios") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(5)
    val b = a.plusHours(16)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
    val ratios = Calculator.getRatios(previous, current)
    
    assert(ratios.size == 3)
    assert(ratios.head.ratio ~= 0.0625)
    assert(ratios.head.tariff == Tariff3)
    
    // 14 / 16 = 0.875
    assert(ratios(1).ratio ~= 0.875)
    assert(ratios(1).tariff == Tariff1)
    
    assert(ratios.last.ratio ~= 0.0625)
    assert(ratios.last.tariff == Tariff2)
  }
  
    test("Monday morning an hour before 6AM and 1 hour after 6AM the next day") {
  
    val a = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(5)
    val b = a.plusHours(26)
    
    val previous = Session("test1", Tick(at = Some(a.getMillis()), odo = 0))
    val current = Session("test1", Tick(at = Some(b.getMillis()), odo = 0))
    val ratios = Calculator.getRatios(previous, current)
    
    assert(ratios.size == 6) // unless we merge similar sequential ratios, but we don't really need to
    // 1 / 26 = 0.03846153846
    assert(ratios(0).ratio ~= 0.03846153846)
    assert(ratios(0).tariff == Tariff3)
    // 14 / 26 = 0.53846153846
    assert(ratios(1).ratio ~= 0.53846153846)
    assert(ratios(1).tariff == Tariff1)
    // 2 / 26 = 0.07692307692
    assert(ratios(2).ratio ~= 0.07692307692)
    assert(ratios(2).tariff == Tariff2)
    // 2 / 26 = 0.07692307692
    assert(ratios(3).ratio ~= 0.07692307692)
    assert(ratios(3).tariff == Tariff3)
    // 6 / 26 = 0.23076923076
    assert(ratios(4).ratio ~= 0.23076923076)
    assert(ratios(4).tariff == Tariff3)
    // 1 / 26 = 0.03846153846
    assert(ratios(5).ratio ~= 0.03846153846)
    assert(ratios(5).tariff == Tariff1)
  }
}