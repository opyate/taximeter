package core


import models.Session
import com.redis.RedisClient
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Instant
import org.joda.time.Interval
import scala.collection.immutable.TreeSet

trait Tariff {
  def getFor(day: Int, hour: Int) = if (day >= 1 && day <= 5) {
    hour match {
      case 0 => Tariff3
      case 6 => Tariff1
      case 20 => Tariff2
      case 22 => Tariff3
    }
  } else { // weekend
    hour match {
      case 0 => Tariff3
      case 6 => Tariff2
      case 22 => Tariff3
    }
  }
  
  /**
   * Example blurb for a tarrif, and then re-written with the variables:
   * 
   * For the first 254.6 metres or 54.8 seconds (whichever is reached first) there is a minimum charge of £2.40
For each additional 127.3 metres or 27.4 seconds (whichever is reached first), or part thereof, if the fare is
less than £17.20 then there is a charge of 20p
Once the fare is £17.20 or greater then there is a charge of 20p for each additional 89.2 metres or 19.2 seconds (whichever is
reached first), or part thereof

For the first $metres metres or $millis seconds (whichever is reached first) there is a minimum charge of £2.40
For each additional $addMetres metres or $addMillis seconds (whichever is reached first), or part thereof, if the fare is
less than $fareThreshold then there is a charge of $charge
Once the fare is $fareThreshold or greater then there is a charge of $charge for each additional $threshAddMetres metres or $threshAddMillis seconds (whichever is
reached first), or part thereof
   */
  def metres: Double
  def millis: Long
  def addMetres: Double
  def addMillis: Long
  def fareThreshold: Double
  // common
  def charge: Double = 20
  def threshAddMetres: Double = 89.2
  def threshAddMillis: Long = 19200
}


case object Tariff1 extends Tariff {
  override def metres: Double = 254.6
  override def millis: Long = 54800
  override def addMetres: Double = 127.3
  override def addMillis: Long = 27400
  override def fareThreshold: Double = 1720
}
case object Tariff2 extends Tariff {
  override def metres: Double = 206.8
  override def millis: Long = 44400
  override def addMetres: Double = 103.4
  override def addMillis: Long = 22200
  override def fareThreshold: Double = 2080
}
case object Tariff3 extends Tariff {
  override def metres: Double = 166.8
  override def millis: Long = 35800
  override def addMetres: Double = 83.4
  override def addMillis: Long = 17900
  override def fareThreshold: Double = 2520
}

case class SwitchTo(when: DateTime, tariff: Tariff)
case class IntervalFor(interval: Interval, fromTariff: Tariff, toTariff: Tariff)

/**
 * A list of interval/tariffId pairs.
 * 
 * We build the timetable for the next few years, and if the server runs that long, perhaps implement
 * a controller which takes care of expiring the timetables. 
 */
object TariffTimetable {
  
  // allows iteration in weeks
  implicit class DateTimeOps (startDt: DateTime) {
    import org.joda.time.Weeks
    def until(endDt: DateTime) = for(number <- 0 until Weeks.weeksBetween(startDt, endDt).getWeeks()) yield(startDt.plusWeeks(number))
  }
  
  /**
   * Calculate a tariff timetable a few years in advance, and take into account a preset
   * list of public holidays.
   * 
   * 0-hour is added as a switch, just in case the previous day was an override day (e.g. a public holiday).
   * Otherwise, it won't have any effect, if for instance one switches to the same tariff.
   */
  def getTimetableWithTariffSwitchEvents(numberOfYears: Int, publicHolidays: List[DateTime]): TreeSet[SwitchTo] = {
    assert(numberOfYears >= 1)
    val overrides = publicHolidays.map(_.withTimeAtStartOfDay())
    
    val now = DateTime.now()
    
    val switches = {
      for {
        date <- now until now.plusYears(numberOfYears)
        day <- List(
          date.withDayOfWeek(DateTimeConstants.MONDAY),
          date.withDayOfWeek(DateTimeConstants.TUESDAY),
          date.withDayOfWeek(DateTimeConstants.WEDNESDAY),
          date.withDayOfWeek(DateTimeConstants.THURSDAY),    
          date.withDayOfWeek(DateTimeConstants.FRIDAY),
          date.withDayOfWeek(DateTimeConstants.SATURDAY),
          date.withDayOfWeek(DateTimeConstants.SUNDAY))
        hour <- List(0, 6, 20, 22)
      } yield {
        val skipOverrides = overrides.contains(day.withTimeAtStartOfDay())
        val skipWeekendsAt8 = hour == 20 && (day.getDayOfWeek().equals(DateTimeConstants.SATURDAY) || day.getDayOfWeek().equals(DateTimeConstants.SUNDAY))
        if (skipOverrides || skipWeekendsAt8) {
          None
        } else {
          Some(SwitchTo(day.withTime(hour, 0, 0, 0), Tariff1.getFor(day.getDayOfWeek(), hour)))
        }
      }
    }
    
    val overrideSwitches = {
      for {
        day <- overrides
      } yield {
        Some(SwitchTo(day.withTime(0, 0, 0, 0), Tariff3))
      }
    }
      
    implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
    val byDateOrdering = Ordering.by{ o: SwitchTo => o.when }
    val set: TreeSet[SwitchTo] = TreeSet((switches ++ overrideSwitches).flatten : _*)(byDateOrdering)
    set
  }


  def getTimetableWithIntervals(numberOfYears: Int, publicHolidays: List[DateTime]) = {
    getTimetableWithTariffSwitchEvents(numberOfYears, publicHolidays).sliding(2, 1).toList.map(_pair => {
      // the tariff is that of the first one
      val pair = _pair.toList
//      println(getFor(pair(1).when.getDayOfWeek(), pair(1).when.getHourOfDay()) + " date " + pair(1).when)
      IntervalFor(new Interval(pair(0).when, pair(1).when), pair(0).tariff, Tariff1.getFor(pair(1).when.getDayOfWeek(), pair(1).when.getHourOfDay()))
    })
  }
}