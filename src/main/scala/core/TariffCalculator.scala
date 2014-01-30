package core

import models.Session
import com.redis.RedisClient
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Instant
import org.joda.time.Interval
import scala.collection.immutable.TreeSet
import akka.dispatch.Foreach
import scala.concurrent.Future


class TariffCalculator(cache: Cache) {

  // TODO too much done in the cache?
  def calc(current: Session): Future[Session] = cache.updateCost(current)

}

/**
 * Stateless calculator (no cache, etc)
 */
object Calculator {
  // TODO load public holidays from configuration
  // TODO, better yet, grab it from somewhere: https://www.gov.uk/bank-holidays.json
  // The below is a shortcut:
  // for D in $(curl https://wpreviousww.gov.uk/bank-holidays.json | jq '.["england-and-wales"].events[] | .date') ; do echo "new DateTime($D)," ; done 
  lazy val publicHolidays = List(
    new DateTime("2012-01-02"),
    new DateTime("2012-04-06"),
    new DateTime("2012-04-09"),
    new DateTime("2012-05-07"),
    new DateTime("2012-06-04"),
    new DateTime("2012-06-05"),
    new DateTime("2012-08-27"),
    new DateTime("2012-12-25"),
    new DateTime("2012-12-26"),
    new DateTime("2013-01-01"),
    new DateTime("2013-03-29"),
    new DateTime("2013-04-01"),
    new DateTime("2013-05-06"),
    new DateTime("2013-05-27"),
    new DateTime("2013-08-26"),
    new DateTime("2013-12-25"),
    new DateTime("2013-12-26"),
    new DateTime("2014-01-01"),
    new DateTime("2014-04-18"),
    new DateTime("2014-04-21"),
    new DateTime("2014-05-05"),
    new DateTime("2014-05-26"),
    new DateTime("2014-08-25"),
    new DateTime("2014-12-25"),
    new DateTime("2014-12-26"),
    new DateTime("2015-01-01"),
    new DateTime("2015-04-03"),
    new DateTime("2015-04-06"),
    new DateTime("2015-05-04"),
    new DateTime("2015-05-25"),
    new DateTime("2015-08-31"),
    new DateTime("2015-12-25"),
    new DateTime("2015-12-28"))
  lazy val intervals = TariffTimetable.getTimetableWithIntervals(10, Nil)
  
  /**
   * Get the tariff intervals applicable to two signals.
   * 
   * E.g.
   * 
   * If two signals doesn't cross tariff boundaries, the returned size below with be 1, of which the only
   * element with be an 
   * 
   * IntervalFor(interval: Interval, fromTariff: Tariff, toTariff: Tariff)
   * 
   * where...
   * interval -> the interval in which the two signals fall
   * fromTariff -> the tariff applicable to the previous.at timestamp
   * toTariff -> the tariff that comes after this tariff
   * 
   * Since the 2 signals fall inside the same interval, they have the same tariff (fromTariff in this case)
   * and toTariff is provided merely for convenience.
   * 
   * E.g.previous
   * 
   * If two signals crosses tariff boundaries, let's say once, then the size below is 2, of which the two elements are:
   * 
   * head -> IntervalFor(interval: Interval, fromTariff: Tariff, toTariff: Tariff)
   * last ->
   * 
   * ...where
   * head.interval -> the interval in which the FIRST signal falls
   * head.fromTariff -> the tariff applicable to the previous.at timestamp
   * head.toTariff -> the tariff that comes after this tariff
   * 
   * ...and
   * 
   * last.interval -> the interval in which the LAST signal falls
   * last.fromTariff -> the tariff applicable to the current.at timestamp
   * last.toTariff -> the tariff that comes after this tariff
   * 
   * ...and 
   * 
   * head.toTariff == last.fromTariff
   */
  def getTariffIntervals(previous: Session, current: Session) = {
    assert(previous.id == current.id)
    
    // get the intervals for the session
    val previousInterval = intervals.find(_.interval.contains(previous.at)).head
    val currentInterval = intervals.find(_.interval.contains(current.at)).head
    // get everything in-between
    val slice = intervals.slice(intervals.indexOf(previousInterval), intervals.indexOf(currentInterval) + 1)
    
    slice
  }
  
  /**
   * Between the last signal and the this signal, if we crossed any tariff boundaries, break the time spent and/or odometer change
   * up into similarly-sized ratios.
   * 
   * E.g.
   * 
   *         previous         cross1                cross2              current              (we don't reach this)
   *    t3 . . . * -----a------ t1 --------b--------- t2 -------c---------- *     . . . . . . . t3
   *    
   * From the above schematic, we want the ratios:
   * 
   * Total = current.at - previous.at (total time spent on journey between signals)
   * Billed @ Tariff3 = a / Total
   * Billed @ Tariff1 = b / Total
   * Billed @ Tariff2 = c / Total
   * 
   * E.g.previous
   * 
   *       previous                       current    (we don't reach this)
   * t3 . . . * ------------ a ------------- * . . . tX
   * 
   * In this case, a == Total
   * The "inbetween" size will be 0.
   */
  def getJourneyTariffLegs(previous: Session, current: Session) = {
    val tariffIntervals = getTariffIntervals(previous, current)
    
    var total: Long = 0
    
    val legs = tariffIntervals.map(intervalFor => {
      
      if (intervalFor.interval.contains(previous.at)) {
        // first interval
        val timeSpent = intervalFor.interval.getEndMillis() - previous.at
        total = total + timeSpent
        Leg(intervalFor, timeSpent)
      } else if (intervalFor.interval.contains(current.at)) {
        // last interval
        val timeSpent = current.at - intervalFor.interval.getStartMillis() 
        total = total + timeSpent
        Leg(intervalFor, timeSpent)
      } else {
        // some middle interval
        val timeSpent = intervalFor.interval.toDurationMillis()
        total = total + timeSpent
        Leg(intervalFor, timeSpent)
      }
    })
    (legs, total)
  }
  
  def getRatios(previous: Session, current: Session) = {
    val (legs, total) = getJourneyTariffLegs(previous, current)
    val ratios = legs.map(leg => {
      Ratio(leg.timeSpent.toDouble / total, leg.interval.fromTariff)
    })
    ratios
  }
  
  /**
   * For the first $metres metres or $millis seconds (whichever is reached first) there is a minimum charge of £2.40
For each additional $addMetres metres or $addMillis seconds (whichever is reached first), or part thereof, if the fare is
less than $fareThreshold then there is a charge of $charge
Once the fare is $fareThreshold or greater then there is a charge of $charge for each additional $threshAddMetres metres or $threshAddMillis seconds (whichever is
reached first), or part thereof

Returns a new session which contains the updated running values.
   */
  def getCosts(previous: Session, current: Session): Session = {
    val ratios = getRatios(previous, current)
    
    val metresSincePrevious = current.odo - previous.odo  
    val millisSincePrevious = current.at - previous.at
    
    var ret = previous.copy(
        at = current.at,
        odo = current.odo,
        runningMillis = previous.runningMillis,
        runningMetres = previous.runningMetres
        )
    
    // now, a piecemeal approach
    var cost = 0.0
    
    ratios.foreach(ratio => {
      var ratioMetresSincePrevious = ratio.ratio * metresSincePrevious
      var ratioMillisSincePrevious = ratio.ratio * millisSincePrevious
      
      // update the advancement according to the ratio of this leg of the journey
      ret = ret.copy(
        runningMillis = ret.runningMillis + ratioMillisSincePrevious.toLong,
        runningMetres = ret.runningMetres + ratioMetresSincePrevious
        )
      
      // "For the first $metres metres or $millis seconds (whichever is reached first) there is a minimum charge of £2.40"
      // but keep in mind that our first session would have been initialised with a running cost of 240p anyway.
      // i.e. if the metres and millis are still under the initial threshold
      val firstFewMetres = ret.runningMetres >= ratio.tariff.metres
      if (firstFewMetres) {
        ratioMetresSincePrevious = ratioMetresSincePrevious - ratio.tariff.metres
      }
      
      val firstFewMillis = ret.runningMillis >= ratio.tariff.millis
      if (firstFewMillis) {
        ratioMillisSincePrevious = ratioMillisSincePrevious - ratio.tariff.millis
      }
        
      if (firstFewMetres || firstFewMillis) {
        
      
        //"For each additional $addMetres metres or $addMillis seconds (whichever is reached first), or part thereof, if the fare is
        // less than $fareThreshold then there is a charge of $charge"
      
        val addMetreCount = ratioMetresSincePrevious / ratio.tariff.addMetres
        val addMillisCount = ratioMillisSincePrevious / ratio.tariff.addMillis
        val count = if (addMetreCount > addMillisCount) addMetreCount else addMillisCount
        var overThreshold = false
        (1 to scala.math.round(count).toInt).foreach(i => {
//          println(s"iter $i : ${ret.runningCost} + ${cost} + ${ratio.tariff.charge} = " + (ret.runningCost + cost + ratio.tariff.charge))
//          println(s"ratio.tariff.fareThreshold = " + ratio.tariff.fareThreshold)
          if (ret.runningCost + cost + ratio.tariff.charge < ratio.tariff.fareThreshold) {
            cost = cost + ratio.tariff.charge
            // reduce the metres and millis in case we need the remainder in the threshold case below
            ratioMetresSincePrevious = ratioMetresSincePrevious - ratio.tariff.addMetres
            ratioMillisSincePrevious = ratioMillisSincePrevious - ratio.tariff.addMillis
          } else {
            // we went over the $fareThreshold
            overThreshold = true
          }
        })
        // "Once the fare is $fareThreshold or greater then there is a charge of $charge for each additional $threshAddMetres metres or
        // $threshAddMillis seconds (whichever is reached first), or part thereof"
        if (overThreshold) {
          val addMetreCount = ratioMetresSincePrevious / ratio.tariff.threshAddMetres
          val addMillisCount = ratioMillisSincePrevious / ratio.tariff.threshAddMillis
          val count = if (addMetreCount > addMillisCount) addMetreCount else addMillisCount
          (1 to scala.math.round(count).toInt).foreach(i => {
            cost = cost + ratio.tariff.charge
          })
        }
      }
    })
    
    ret = ret.copy(runningCost = previous.runningCost + cost)
    
    ret
  }
}

case class Leg(interval: IntervalFor, timeSpent: Long)
case class Ratio(ratio: Double, tariff: Tariff)