package org.unfairfunction.smartsox.things.door

import org.unfairfunction.smartsox.actors.ThingsManager
import akka.actor.{Props, Actor, ActorLogging}
//import org.unfairfunction.smartsox.actors.Thing.{GetState, Die}
import org.unfairfunction.smartsox.things.door.Door.{GetState, Die, OpenDoor, CloseDoor}
import akka.actor.ActorRef

//import org.unfairfunction.smartsox.things.door.Door.{OpenDoor, CloseDoor}

object DoorsManager {
  import ThingsManager._
  
  trait Command
  case class GetState(doorId: String) extends Command
  case class AddDoor(doorId: String) extends Command
  case class RemoveDoor(doorId: String) extends Command
  case class Open(doorId: String) extends Command
  case class Close(doorId: String) extends Command
  
  def props(id: String) = Props(new DoorsManager)
}

//class DoorsManager extends ThingsManager {
  import ThingsManager._
  import DoorsManager._
class DoorsManager extends Actor with ActorLogging {
  
  log.debug(s"DoorsManager ${context.self.path.name}  created")
  
  def receive =  {
    case DoorsManager.GetState(id) => sendToChild(id, Door.GetState)
    case DoorsManager.AddDoor(id) => sendToChild(id, Door.GetState)
    case DoorsManager.RemoveDoor(id) => sendToChild(id, Door.Die)
//    case DoorsManager.RemoveDoor(id) => context stop findOrCreate(id)
    case DoorsManager.Open(id) => sendToChild(id, Door.OpenDoor)
    case DoorsManager.Close(id) => sendToChild(id, Door.CloseDoor)    
  }
  
  def sendToChild(thingId: String, command: Door.Command) = {
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

    
  def props(id: String) = DoorsManager.props(id)
  
  def thingProps(id: String) = Door.props(id)
}