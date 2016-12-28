package org.unfairfunction.smartsox.things.door

import akka.actor.Props
import akka.persistence.SnapshotMetadata
import akka.persistence.fsm.PersistentFSM.FSMState
import akka.persistence.fsm.PersistentFSM
import scala.reflect.ClassTag
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Send}
import org.json4s.native.Serialization
import org.json4s.NoTypeHints
import org.json4s.DefaultFormats
import org.json4s.FieldSerializer
import org.unfairfunction.smartsox.actors.ThingActor
import org.unfairfunction.smartsox.actors.Thing
import org.unfairfunction.smartsox.actors.ThingActor.{GetState, GetThing}

object DoorActor {
//  import Thing._
  trait State extends ThingActor.State
  case object Opened extends State {
    override def identifier: String = "opened"
  }
  case object Closed extends State {
    override def identifier: String = "closed"
  }
  case object Locked extends State {
    override def identifier: String = "locked"
  }
  case object Opening extends State {
    override def identifier: String = "opening"
  }
  case object Closing extends State {
    override def identifier: String = "closing"
  }
  case object Locking extends State {
    override def identifier: String = "locking"
  }
  case object Unlocking extends State {
    override def identifier: String = "unlocking"
  }
  
  trait Command extends ThingActor.Command
//  case object GetState extends Command
  case object OpenDoor extends Command
  case object CloseDoor extends Command
  case object LockDoor extends Command
  case object UnlockDoor extends Command
  case object Die extends Command
  
  trait DomainEvent extends ThingActor.DomainEvent
  case object DoorOpening extends DomainEvent
  case object DoorClosing extends DomainEvent
  case object DoorOpened extends DomainEvent
  case object DoorClosed extends DomainEvent
  case object DoorLocking extends DomainEvent
  case object DoorUnlocking extends DomainEvent
  case object DoorLocked extends DomainEvent
  
  trait CommandFailed extends ThingActor.CommandFailed
  case class DoorClosingFailed(reason: String) extends CommandFailed
  case class DoorOpeningFailed(reason: String) extends CommandFailed
  case class DoorLockingFailed(reason: String) extends CommandFailed
  case class DoorUnlockingFailed(reason: String) extends CommandFailed
  
  trait Data extends ThingActor.Data
  case object EmptyData extends Data
  case class DoorData() extends Data
  
  
  def props(door: Door): Props = Props(new DoorActor(door))
}

//class DoorActor(implicit val domainEventClassTag: ClassTag[DoorActor.DomainEvent]) extends PersistentFSM[DoorActor.State, DoorActor.Data, DoorActor.DomainEvent] {
class DoorActor(override val myThing: Door)(implicit val doorDomainEventClassTag: ClassTag[DoorActor.DomainEvent]) extends ThingActor {
  import DoorActor._
  
  val persistenceId: String = self.path.name

//  val mediator = DistributedPubSub(context.system).mediator
  
  val toJson = 
  
  log.info(s"door $persistenceId created")

  startWith(Opened, EmptyData)
  
  override def applyEvent(domainEvent: ThingActor.DomainEvent, currentData: ThingActor.Data): ThingActor.Data = {
    domainEvent match {
      case DoorOpening => currentData
      case DoorClosing => currentData
      case DoorOpened => currentData
      case DoorClosed => currentData
      case DoorLocking => currentData
      case DoorUnlocking => currentData
      case DoorLocked => currentData
    }
  }
  
  when (Opened) {
    case Event(GetState, _) =>
      stay replying Opened
    case Event(GetThing, _) =>
      stay replying myThing
    case Event(CloseDoor, _) =>
      goto (Closing) applying DoorClosing andThen {
        case _ => {
          mediator ! Publish(persistenceId, Closing)
          closeDoor
        }
      }


    case Event(Die, _) =>
      stop()
    case evt =>
      stay replying Opened andThen {
        case _ => log.debug(s"door $persistenceId, received event $evt and not sure what to do. ignoring")
      }
  }
  
  when (Closed) {
    case Event(GetState, _) =>
      stay replying Closed
    case Event(GetThing, _) =>
      stay replying myThing
    case Event(OpenDoor, _) =>
      goto (Opening) applying DoorOpening andThen {
        case _ => {
          mediator ! Publish(persistenceId, Opening)
          openDoor
//          sender ! Opened
        }
      } 
    case Event(LockDoor, _) =>
      goto (Locking) applying DoorLocking andThen {
        case _ => {
          mediator ! Publish(persistenceId, Locking)
          lockDoor
        }
      }
    case Event(Die, _) =>
      stop()
    case evt =>
      stay replying Closed andThen {
        case _ => log.debug(s"door $persistenceId, received event $evt and not sure what to do. ignoring")
      }
  }
  
  when (Closing) {
    case Event(GetState, _) =>
      stay replying Closing
    case Event(GetThing, _) =>
      stay replying myThing
    case Event(DoorClosed, _) =>
      goto (Closed) applying DoorClosed andThen {
        case _ =>
          mediator ! Publish(persistenceId, Closed)
      }


    case Event(Die, _) =>
      stop()
    case evt =>
      stay replying Closing andThen {
        case _ => log.debug(s"door $persistenceId, received event $evt and not sure what to do. ignoring")
      }
  }
  
  when (Opening) {
    case Event(GetState, _) =>
      stay replying Opening
    case Event(GetThing, _) =>
      stay replying myThing
    case Event(DoorOpened, _) =>
      goto (Opened) applying DoorOpened andThen {
        case _ => {
          mediator ! Publish(persistenceId, Opened)
        }
      }


    case Event(Die, _) =>
      stop()
    case evt =>
      stay replying Opening andThen {
        case _ => log.debug(s"door $persistenceId, received event $evt and not sure what to do. ignoring")
      }
  }
  
  when (Locking) {
    case Event(GetState, _) =>
      stay replying Locking
    case Event(GetThing, _) =>
      stay replying myThing
    case Event (DoorLocked, _) =>
      goto (Locked) applying DoorLocked andThen {
        case _ => {
          mediator ! Publish(persistenceId, Locked)
        }
      }


    case Event(Die, _) =>
      stop()
    case evt =>
      stay replying Locking andThen {
        case _ => log.debug(s"door $persistenceId, received event $evt and not sure what to do. ignoring")
      }
  }
  
  when (Unlocking) {
    case Event(GetState, _) =>
      stay replying Unlocking
    case Event(GetThing, _) =>
      stay replying myThing
    case Event(DoorClosed, _) =>
      goto (Closed) applying DoorClosed andThen {
        case _ => {
          mediator ! Publish(persistenceId, Closed)
        }
      }


    case Event(Die, _) =>
      stop()
    case evt =>
      stay replying Unlocking andThen {
        case _ => log.debug(s"door $persistenceId, received event $evt and not sure what to do. ignoring")
      }
  }
  
  when (Locked) {
    case Event(GetState, _) =>
      stay replying Locked
    case Event(GetThing, _) =>
      stay replying myThing
    case Event (UnlockDoor, _) =>
      goto (Unlocking) applying DoorUnlocking andThen {
        case _ => {
          mediator ! Publish(persistenceId, Unlocking)
          unlockDoor
        }
      }


    case Event(Die, _) =>
      stop()
    case evt =>
      stay replying Locked andThen {
        case _ => {
          log.debug(s"door $persistenceId, received event $evt and not sure what to do. ignoring")
//          mediator ! Send(sender.path.toString(), Locked, true)
        }
      }
  }

  private def closeDoor = {
    log.debug(s"door $persistenceId, closing door")
    self ! DoorClosed
  }
  
  private def openDoor = {
    log.debug(s"door $persistenceId, opening door")
    self ! DoorOpened
  }
  
  private def lockDoor = {
    log.debug(s"door $persistenceId, locking door")
    self ! DoorLocked
  }
  
  private def unlockDoor = {
    log.debug(s"door $persistenceId, unlocking door")
    self ! DoorClosed
  }
}