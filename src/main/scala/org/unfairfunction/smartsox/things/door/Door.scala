package org.unfairfunction.smartsox.things.door

import org.unfairfunction.smartsox.actors.Thing
import akka.actor.Props
import akka.persistence.SnapshotMetadata
import org.unfairfunction.smartsox.actors.Thing.{GetState, Event, Uninitialized}
import scala.util.Random

object Door {
  import Thing._
  
  case object Opened extends State
  case object Closed extends State
  case object Opening extends State
  case object Closing extends State
  
  case object OpenDoor extends Command
  case object CloseDoor extends Command
  
  case object DoorOpening extends Event
  case object DoorClosing extends Event
  case object DoorOpened extends Event
  case object DoorClosed extends Event
  case class DoorClosingFailed(reason: String) extends CommandFailed
  case class DoorOpeningFailed(reason: String) extends CommandFailed
  
  
  def props(id: String): Props = Props(new Door(id))
}

class Door(val persistenceId: String) extends Thing {
  import Door._
  
//  Random.nextBoolean() match {
//    case true => self ! OpenDoor
//    case false => self ! CloseDoor
//  }
  
  this.state = Uninitialized
  log.debug(s"door $persistenceId created")
  
  protected def restoreFromSnapshot(metadata: SnapshotMetadata,state: Thing.State): Unit = {
    this.state = state
    state match {
      case Opened => context become opened
      case Closed => context become closed
      case Opening => context become opening
      case Closing => context become closing
      case _ => context become initial
    }
  }
  
  def updateState(evt: Event): Unit = {
    evt match {
      case DoorClosed => {
        context become closed
        state = Closed
      }      
      case DoorOpened => {
        context become opened
        state = Opened
      }      
      case DoorClosing => {
        context become closing
        state = Closing
      }      
      case DoorOpening => {
        context become opening
        state = Opening
      }
      case _ => {
        context become initial
        state = Uninitialized
      }
    }
  }
  
  val initial: Receive = {
    case GetState => {
      respond()
    }
    case OpenDoor => {
      log.info("opening door")
      persist(DoorOpening)(afterEventPersisted)
      openDoor 
    }
    case CloseDoor => {
      log.info("closing door")
      persist(DoorClosing)(afterEventPersisted)
      closeDoor 
    }
  }
  
  val opened: Receive = {
    case GetState => {
      respond()
    }
    case CloseDoor => {
      log.info("closing door")
      persist(DoorClosing)(afterEventPersisted)
      closeDoor 
    }
  }
  
  val closed: Receive = {
    case GetState => {
      respond()
    }
    case OpenDoor => {
      log.info("opening door")
      persist(DoorOpening)(afterEventPersisted)
      openDoor 
    }
  }
  
  val opening: Receive = {
    case GetState => {
      respond()
    }
    case DoorOpened => {
      log.info("door opened")
      persist(DoorOpened)(afterEventPersisted)
    }
    case DoorOpeningFailed(msg) => {
      log.error(s"door failed opening with message: $msg")
      persist(DoorOpeningFailed(msg))(afterEventPersisted)
    }
  }
  
  val closing: Receive = {
    case GetState => {
      respond()
    }
    case DoorClosed => {
      log.info("door closed")
      persist(DoorClosed)(afterEventPersisted)
    }
    case DoorClosingFailed(msg) => {
      log.error(s"door failed closing with message: $msg")
      persist(DoorClosingFailed(msg))(afterEventPersisted)
    }
  }
  
  override val receiveCommand: Receive = initial
  
  private def closeDoor = {
//    Random.nextBoolean match {
//      case true => self ! DoorClosed
//      case false => self ! DoorClosingFailed("Something obstracting the door from closing")
//      case true => {
        state = Closed
        context become closed
//      }
//      case false => self ! DoorClosingFailed("Something obstracting the door from closing")
//    }
  }
  
  private def openDoor = {
    state = Opened
    context become opened
//    Random.nextBoolean match {
//      case true => self ! DoorOpened
//      case false => self ! DoorOpeningFailed("Something obstracting the door from closing")
//    }    
  }
}