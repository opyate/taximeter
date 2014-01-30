package core

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

trait Fixtures {

  // I can't really use "now" now, otherwise those public holidays will creep up on me...
  lazy val now = new DateTime("2014-01-29")

  val jodaWeekdays = List(
    DateTimeConstants.MONDAY,
    DateTimeConstants.TUESDAY,
    DateTimeConstants.WEDNESDAY,
    DateTimeConstants.THURSDAY,
    DateTimeConstants.FRIDAY)
  
  
  val jodaWeekendDays = List(
    DateTimeConstants.SATURDAY,
    DateTimeConstants.SUNDAY)
    
}