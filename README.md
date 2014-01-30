# Taximeter web API

Based on tariffs as per [the TFL website](http://www.tfl.gov.uk/gettingaround/taxisandminicabs/taxis/taxifares/4870.aspx) valid from 6 April 2013.

WARNING: this is a work in progress. Please don't use it to power your cab company.

# Benefit of this system

It doesn't require regular pulses and a perfectly calibrated/synced clock. It will take your start and end times and estimate the cost as best as possible, taking into account tariff boundaries.

Also, a simulation can be run very quickly by just firing requests with disparate timestamps off in rapid succession -- it doesn't rely on the current time in you specify your own timestamps.

# API

## GET /api/session?odo=${ODO_METRES}&at=${TIMESTAMP_MILLIS}

### Parameters

**ODO_METRES** the current odometer reading in metres

**TIMESTAMP_MILLIS** (optional) the timestamp at which the session starts, or "now"

### Example JSON response

    {
        id: "c38f3052-d4e1-4a1f-ade2-7c65fad25513",
        at: 1391085370860,
        odo: 0,
        runningCost: 240,
        runningMillis: 0,
        runningMetres: 0
    }

## GET /api/tariff/${SESSION_TOKEN}?odo=${ODO_METRES}&at=${TIMESTAMP_MILLIS}

### Parameters

**SESSION_TOKEN** the UUID session token from the /api/session response

**ODO_METRES** the current odometer reading in metres

**TIMESTAMP_MILLIS** (optional) the timestamp at which the session continues, or "now"

### Example JSON response

    {
        id: "1373a754-1821-4233-8a8e-2de724204f48",
        at: 1391087523371,
        odo: 900,
        runningCost: 340,
        runningMillis: 1000,
        runningMetres: 900
    }

The running cost has now been updated according to the tariff engine.

The database now also has the following 2 entries for this session:

    redis 127.0.0.1:6379> lrange tm:session:1373a754-1821-4233-8a8e-2de724204f48 0 10
    1) "{\"id\":\"1373a754-1821-4233-8a8e-2de724204f48\",\"at\":1391087523371,\"odo\":900.0,\"runningCost\":340.0,\"runningMillis\":1000,\"runningMetres\":900.0}"
    2) "{\"id\":\"1373a754-1821-4233-8a8e-2de724204f48\",\"at\":1391087522371,\"odo\":0.0,\"runningCost\":240.0,\"runningMillis\":0,\"runningMetres\":0.0}"

A subsequent call to http://localhost:3001/api/tariff/1373a754-1821-4233-8a8e-2de724204f48?odo=1900&at=1391087533371 yields

    {
        id: "1373a754-1821-4233-8a8e-2de724204f48",
        at: 1391087533371,
        odo: 1900,
        runningCost: 500,
        runningMillis: 11000,
        runningMetres: 1900
    }

...and this in the database:

    redis 127.0.0.1:6379> lrange tm:session:1373a754-1821-4233-8a8e-2de724204f48 0 10
    1) "{\"id\":\"1373a754-1821-4233-8a8e-2de724204f48\",\"at\":1391087533371,\"odo\":1900.0,\"runningCost\":500.0,\"runningMillis\":11000,\"runningMetres\":1900.0}"
    2) "{\"id\":\"1373a754-1821-4233-8a8e-2de724204f48\",\"at\":1391087523371,\"odo\":900.0,\"runningCost\":340.0,\"runningMillis\":1000,\"runningMetres\":900.0}"
    3) "{\"id\":\"1373a754-1821-4233-8a8e-2de724204f48\",\"at\":1391087522371,\"odo\":0.0,\"runningCost\":240.0,\"runningMillis\":0,\"runningMetres\":0.0}"

# The tariffs, and when they apply

<TABLE CELLSPACING="0" COLS="9" BORDER="0">
	<COLGROUP SPAN="9" WIDTH="85"></COLGROUP>
	<TR>
		<TD HEIGHT="16" ALIGN="LEFT"><BR></TD>
		<TD ALIGN="LEFT">Monday</TD>
		<TD ALIGN="LEFT">Tuesday</TD>
		<TD ALIGN="LEFT">Wednesday</TD>
		<TD ALIGN="LEFT">Thursday</TD>
		<TD ALIGN="LEFT">Friday</TD>
		<TD ALIGN="LEFT">Saturday</TD>
		<TD ALIGN="LEFT">Sunday</TD>
		<TD ALIGN="LEFT">Public Holiday</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0" SDNUM="2057;0;HH:MM:SS">00:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.0416666666666667" SDNUM="2057;0;HH:MM:SS">01:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.0833333333333333" SDNUM="2057;0;HH:MM:SS">02:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.125" SDNUM="2057;0;HH:MM:SS">03:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.166666666666667" SDNUM="2057;0;HH:MM:SS">04:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.208333333333333" SDNUM="2057;0;HH:MM:SS">05:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.25" SDNUM="2057;0;HH:MM:SS">06:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.291666666666667" SDNUM="2057;0;HH:MM:SS">07:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.333333333333333" SDNUM="2057;0;HH:MM:SS">08:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.375" SDNUM="2057;0;HH:MM:SS">09:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.416666666666667" SDNUM="2057;0;HH:MM:SS">10:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.458333333333333" SDNUM="2057;0;HH:MM:SS">11:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.5" SDNUM="2057;0;HH:MM:SS">12:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.541666666666667" SDNUM="2057;0;HH:MM:SS">13:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.583333333333333" SDNUM="2057;0;HH:MM:SS">14:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.625" SDNUM="2057;0;HH:MM:SS">15:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.666666666666667" SDNUM="2057;0;HH:MM:SS">16:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.708333333333333" SDNUM="2057;0;HH:MM:SS">17:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.75" SDNUM="2057;0;HH:MM:SS">18:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.791666666666667" SDNUM="2057;0;HH:MM:SS">19:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.833333333333333" SDNUM="2057;0;HH:MM:SS">20:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="1" SDNUM="2057;">1</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.875" SDNUM="2057;0;HH:MM:SS">21:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.916666666666667" SDNUM="2057;0;HH:MM:SS">22:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="2" SDNUM="2057;">2</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
	<TR>
		<TD HEIGHT="16" ALIGN="RIGHT" SDVAL="0.958333333333333" SDNUM="2057;0;HH:MM:SS">23:00:00</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
		<TD ALIGN="RIGHT" SDVAL="3" SDNUM="2057;">3</TD>
	</TR>
</TABLE>

# The calculator

In simple terms, the session (the moment the flag falls) starts at a certain time T and odometer reading N.

The time T can fall into one of the 3 tariff categories as per [the TFL website](http://www.tfl.gov.uk/gettingaround/taxisandminicabs/taxis/taxifares/4870.aspx).

Hence, the session will be associated with a Tariff on initialisation.

For said session, the calculator can now subsequently be asked for the current tariff T+t, or N+n.

T+t brings the session to a time in the future, a time after T.
N+n brings the odometer to a higher reading, indicating that some distance has now been covered.

T+t == T for no time spent in session.
N+n == N for no distance covered in session.

It is possible for time to have passed, but no distance (e.g. "Can we just sit in your cab? I don't mind paying."). It is also possible for distance to have been covered, but no time have passed (e.g. the cab teleported to a new location instantaneously. I mean, how great would that be?)

A calculation request at T+t can put the session in a new tariff altogether (see table above), as per "If a journey goes through more than one tariff, the new charge will be applied from the start time of the new tariff."

In summary, the best the calculator can do is provide a calculation based on what you tell it: the session starting at T and N, and multiple subsequent T+t, N+n.

So, the calculator can know one or many T/N pairs.

## One

    Set(Tick(at=T, odo=N))

The cost of this session as it stands is 240p

## Many

    Set(
      Tick(at=T,     odo=N)
      Tick(at=T+t1,  odo=N + n1),
      Tick(at=T+t2,  odo=N + n2),
      Tick(at=T+t.., odo=N + n..),
      Tick(at=T+tu,  odo=N + nm))
      
The cost of this session is at a minimum 240p, and for every tick we add extra cost based on certain factors as described in [the TFL website](http://www.tfl.gov.uk/gettingaround/taxisandminicabs/taxis/taxifares/4870.aspx).

If a tick falls in a new tariff, we have to work backwards.

