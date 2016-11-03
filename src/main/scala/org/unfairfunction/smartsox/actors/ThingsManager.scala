package org.unfairfunction.smartsox.actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import akka.actor.Terminated
import java.util.Calendar

object ThingsManager {

  trait Command {
    protected val createTime: Calendar = Calendar.getInstance
  }
  
  trait Event {
    protected val createTime: Calendar = Calendar.getInstance
  }
  
  case class Retire(id: String) extends Command
  case class Unretire(id: String) extends Command
  
  val maxThings = 40
  val thingsToKillAtOnce = 20
  
}

trait ThingsManager extends Actor with ActorLogging {
  import Thing._
  import ThingsManager._
  
  
  def processCommand: Receive

  override def receive = processCommand orElse defaultProcessCommand

  private def defaultProcessCommand: Receive = {
    case t@Terminated(actor) => {
      log.info(s"actor $actor terminated. sending to parent ${context.parent.path.name}")
      context.parent ! t
    }
    case ThingsManager.Retire(id) => {
      processThingCommand(id, Thing.Retire)
    }
    case ThingsManager.Unretire(id) => {
      processThingCommand(id, Thing.Unretire)
    }
  }
  
  def processThingCommand(thingId: String, command: Thing.Command) = {
//    val maybeChild = context child thingId
//    maybeChild match {
//      case Some(child) =>
//        child forward command
//      case None =>
//        val child = create(thingId)
//        child forward command
//    }
    val child = findOrCreate(thingId)
    child forward command
    
  }

  protected def findOrCreate(id: String): ActorRef =
    context child id getOrElse create(id)

  protected def create(id: String): ActorRef = {
    val nextThing = context.actorOf(thingProps(id), id)
    context watch nextThing
    nextThing
  }
  
  def thingProps(id: String): Props
  
  def props(id: String): Props
}