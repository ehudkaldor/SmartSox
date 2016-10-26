package org.unfairfunction.smartsox.things.door

import org.unfairfunction.smartsox.actors.ThingsManager
import akka.actor.Props
import org.unfairfunction.smartsox.actors.Thing.{GetState, Die}
import org.unfairfunction.smartsox.things.door.Door.{OpenDoor, CloseDoor}

object DoorsManager {
  import ThingsManager._
  
  case class AddDoor(doorId: String) extends Command
  case class RemoveDoor(doorId: String) extends Command
  case class Open(doorId: String) extends Command
  case class Close(doorId: String) extends Command
  
  def props(id: String) = Props(new DoorsManager)
}

class DoorsManager extends ThingsManager {
  import DoorsManager._
  
  override def processCommand = {
    case AddDoor(id) => processThingCommand(id, GetState)
    case RemoveDoor(id) => processThingCommand(id, Die)
    case Open(id) => processThingCommand(id, OpenDoor)
    case Close(id) => processThingCommand(id, CloseDoor)    
  }
    
  override def props(id: String) = DoorsManager.props(id)
}