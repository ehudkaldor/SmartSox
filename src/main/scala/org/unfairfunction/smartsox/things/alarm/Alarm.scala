package org.unfairfunction.smartsox.things.alarm

import org.unfairfunction.smartsox.actors.Thing
import akka.actor.Props
import akka.persistence.SnapshotMetadata
import scala.collection.mutable.Map

object Alarm {
  import Thing.{Command, Event, State}
  
//  trait AlarmState extends State {
//    val triggerList = Map.empty[String, String]
//  }
  
  case class Armed() extends State
  case class Arming() extends State
  case class Disarmed() extends State
  case class Disarming() extends State
  case class Triggered() extends State
  
  case object ArmAlarm extends Command
  case object DisarmAlarm extends Command
  case class AddTrigger(val listenFor: String) extends Command
  case class RemoveTrigger(val listenFor: String) extends Command
  
  case object AlarmArmed extends Event
  case object AlarmArming extends Event
  case object AlarmDisarmed extends Event
  case object AlarmDisarming extends Event
  case class TriggerAdded(val listenFor: String) extends Event
  case class TriggerRemoved(val listenFor: String) extends Event
  
  
  def props(id: String): Props = Props(new Alarm(id))
}

class Alarm(val persistenceId: String) extends Thing {
  
  val triggerList = Map.empty[String, String]
  
  import Alarm._
  import Thing.{Uninitialized, GetState, Die, Event}
  
  val initial: Receive = {
    case GetState => {
      log.debug(s"alarm $persistenceId, state $state, received GetState")
      respond()
    }
    case ArmAlarm => {
      log.debug(s"door $persistenceId, state $state, received ArmAlarm")
      persist(AlarmArming)(afterEventPersisted)
      armAlarm 
    }
    case e: AddTrigger => {
      log.debug(s"alarm $persistenceId, state $state, received AddTrigger of ${e.listenFor}")
      persist(TriggerAdded(e.listenFor))(afterEventPersisted)
    }
    case e: RemoveTrigger => {
      log.debug(s"alarm $persistenceId, state $state, received RemoveTrigger of ${e.listenFor}")
      persist(TriggerRemoved(e.listenFor))(afterEventPersisted)
    }
    case Die => {
      log.debug(s"door $persistenceId, got command to Die. Dying")
      context stop self
    }
    case evt => {
//      log.debug(s"alarm $persistenceId, state $state, received event $evt and not sure what to do. ignoring")
      respond()
    }
  }
  
  val arming: Receive = {
    case GetState => {
      log.debug(s"alarm $persistenceId, state $state, received GetState")
      respond()
    }
    case AlarmArmed => {
      log.debug(s"door $persistenceId, state $state, received AlarmArmed")
      persist(AlarmArmed)(afterEventPersisted)
    }
    case Die => {
      log.debug(s"door $persistenceId, got command to Die. Dying")
      context stop self
    }
    case evt => {
//      log.debug(s"alarm $persistenceId, state $state, received event $evt and not sure what to do. ignoring")
      respond()
    }
  }
  
  val armed: Receive = {
    ???
  }
  
  val disarming: Receive = {
    ???
  }
    
  val disarmed: Receive = {
    ???
  }

  override val receiveCommand: Receive = initial

  protected def restoreFromSnapshot(metadata: SnapshotMetadata, state: Thing.State): Unit = {
    this.state = state
    state match {
      case Armed() => context become armed
      case Arming() => context become arming
      case Disarmed() => context become disarmed
      case Disarming() => context become disarming
      case _ => context become initial
    }
  }
  
  def updateState(evt: Event): Unit = {
    evt match {
      case evt: TriggerAdded => {
        triggerList += (evt.listenFor -> "")
      }
      case evt: TriggerRemoved => {
        triggerList -=  evt.listenFor
      }
      case AlarmArmed => {
        context become armed
        state = Armed()
      }
      case AlarmArming => {
        context become arming
        state = Arming()
      }
      case AlarmDisarmed => {
        context become disarmed
        state = Disarmed()
      }
      case AlarmDisarming => {
        context become disarming
        state = Disarming()
      }
      case _ => {
        context become initial
        state = Uninitialized
      }
    log.debug(s"alarm $persistenceId, state updated to $state")
    }
  }
  
  private def armAlarm = {
    log.debug(s"alarm $persistenceId, arming alarm")
    self ! AlarmArmed
  }
  
  private def disarmAlarm = {
    log.debug(s"alarm $persistenceId, disarming alarm")
    self ! AlarmDisarmed   
  }
}