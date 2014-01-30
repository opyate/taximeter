package core

import com.redis.RedisClient
import models.Session
import models.Tick
import akka.util.Timeout
import com.redis.serialization.DefaultFormats
import com.redis.serialization.Format
import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success

class Cache(db: RedisClient)(implicit executionContext: ExecutionContext) {
  
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)
  
  import DefaultFormats._
  
  
  // store Session in Redis
  import com.redis.serialization.SprayJsonSupport._
  import models.JsonProtocol._

  def newSession(start: Tick) = {
    val session = Session(start)
    
    // Add one or more members to a set
    db.sadd("tm:session", session.id)
    
    // Set the value of a key, only if the key does not exist
    db.lpush("tm:session:" + session.id, session)
    session
  }
  
  def updateCost(current: Session) = {
    db.lrange[Session]("tm:session:" + current.id, -1, -1) map { result =>
    	val previous :: Nil = result
        println(s"Found pprevious session = $previous")
        assert(previous.id == current.id)
        val newSession = Calculator.getCosts(previous, current)
        println(s"Calculated new session = $newSession")
        assert(newSession.id == current.id)
        db.lpush("tm:session:" + current.id, newSession)
        newSession
    }
  }
}