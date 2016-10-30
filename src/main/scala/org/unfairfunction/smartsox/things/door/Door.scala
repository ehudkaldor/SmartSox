package org.unfairfunction.smartsox.things.door

import org.unfairfunction.smartsox.actors.Thing
import akka.actor.Props
import akka.persistence.SnapshotMetadata
import org.unfairfunction.smartsox.actors.Thing.{GetState, Event, Uninitialized}
import scala.util.Random
import org.unfairfunction.smartsox.actors.Thing.Die

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
    log.debug(s"state updated to $state")
  }
  
  val initial: Receive = {
    case GetState => {
      log.debug(s"door $persistenceId, state $state, received GetState")
      respond()
    }
    case OpenDoor => {
      log.debug(s"door $persistenceId, state $state, received OpenDoor")
      persist(DoorOpening)(afterEventPersisted)
      openDoor 
    }
    case CloseDoor => {
      log.debug(s"door $persistenceId, state $state, received CloseDoor")
      persist(DoorClosing)(afterEventPersisted)
      closeDoor 
    }
    case DoorOpening => {
      log.debug(s"got event DoorOpening in state $state, meaning some opened the door manually")
      persist(DoorOpening)(afterEventPersisted)
    }
    case DoorClosing => {
      log.debug(s"got event DoorClosing in state $state, meaning some closed the door manually")
      persist(DoorClosing)(afterEventPersisted)
    }
    case Die => {
      log.debug(s"door $persistenceId, got command to Die. Dying")
      context stop self
    }
    case evt => {
//      log.debug(s"door $persistenceId, state $state, received event $evt and not sure what to do. ignoring")
    }
  }
  
  val opened: Receive = {
    case GetState => {
      log.debug(s"door $persistenceId, state $state, received GetState")
      respond()
    }
    case CloseDoor => {
      log.debug(s"door $persistenceId, state $state, received CloseDoor")
      persist(DoorClosing)(afterEventPersisted)
      closeDoor 
    }
    case DoorClosing => {
      log.debug(s"got event DoorClosing in state $state, meaning some closed the door manually")
      persist(DoorClosing)(afterEventPersisted)
    }
    case Die => {
      log.debug(s"door $persistenceId, got command to Die. Dying")
      context stop self
    }
    case evt => {
//      log.debug(s"door $persistenceId, state $state, received event $evt and not sure what to do. ignoring")
    }
  }
  
  val closed: Receive = {
    case GetState => {
      log.debug(s"door $persistenceId, state $state, received GetState")
      respond()
    }
    case OpenDoor => {
      log.debug(s"door $persistenceId, state $state, received OpenDoor")
      persist(DoorOpening)(afterEventPersisted)
      openDoor 
    }
    case DoorOpening => {
      log.debug(s"got event DoorOpening in state $state, meaning some opened the door manually")
      persist(DoorOpening)(afterEventPersisted)
    }
    case Die => {
      log.debug(s"door $persistenceId, got command to Die. Dying")
      context stop self
    }
    case evt => {
//      log.debug(s"door $persistenceId, state $state, received event $evt and not sure what to do. ignoring")
    }
  }
  
  val opening: Receive = {
    case GetState => {
      log.debug(s"door $persistenceId, state $state, received GetState")
      respond()
    }
    case DoorOpened => {
      log.debug(s"door $persistenceId,state $state, received DoorOpened")
      persist(DoorOpened)(afterEventPersisted)
    }
    case DoorOpeningFailed(msg) => {
      log.error(s"door $persistenceId, state $state, door failed to open with message: $msg")
      persist(DoorOpeningFailed(msg))(afterEventPersisted)
    }
    case Die => {
      log.debug(s"door $persistenceId, got command to Die. Dying")
      context stop self
    }
    case evt => {
//      log.debug(s"door $persistenceId, state $state, received event $evt and not sure what to do. ignoring")
    }
  }
  
  val closing: Receive = {
    case GetState => {
      log.debug(s"door $persistenceId, state $state, received GetState")
      respond()
    }
    case DoorClosed => {
      log.debug(s"door $persistenceId, state $state, received DoorClosed")
      persist(DoorClosed)(afterEventPersisted)
    }
    case DoorClosingFailed(msg) => {
      log.error(s"door $persistenceId, state $state, door failed tp close with message: $msg")
      persist(DoorClosingFailed(msg))(afterEventPersisted)
    }
    case Die => {
      log.debug(s"door $persistenceId, got command to Die. Dying")
      context stop self
    }
    case evt => {
//      log.debug(s"door $persistenceId, state $state, received event $evt and not sure what to do. ignoring")
    }
  }
  
  override val receiveCommand: Receive = initial
  
  private def closeDoor = {
    log.debug(s"door $persistenceId, closing door")
    self ! DoorClosed
  }
  
  private def openDoor = {
    log.debug(s"door $persistenceId, opening door")
    self ! DoorOpened
  }
}