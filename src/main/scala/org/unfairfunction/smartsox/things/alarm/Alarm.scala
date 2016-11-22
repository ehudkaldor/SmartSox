package org.unfairfunction.smartsox.things.alarm

//import org.unfairfunction.smartsox.actors.Thing
import akka.actor.Props
import akka.persistence.SnapshotMetadata
//import scala.collection.mutable.Map
//import org.unfairfunction.smartsox.actors.Thing.Data
import scala.concurrent.duration.DurationInt
//import org.unfairfunction.smartsox.actors.Thing.EmptyData
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import scala.reflect.ClassTag
//import org.unfairfunction.smartsox.actors.Thing.EmptyData

object Alarm {
//  import Thing.{Command, DomainEvent, State}
  
//  trait AlarmState extends State {
//    val triggerList = Map.empty[String, String]
//  }
  
  trait State extends FSMState  
  case object Armed extends State {
    override def identifier: String = "armed"
  }
  case object Arming extends State {
    override def identifier: String = "arming"
  }
  case object Disarmed extends State {
    override def identifier: String = "disarmed"
  }
  case object Disarming extends State {
    override def identifier: String = "disarming"
  }
  case object Triggered extends State {
    override def identifier: String = "triggered"
  }
  
  trait Command  
  case object GetState extends Command
  case object Die extends Command
  case object ArmAlarm extends Command
  case object DisarmAlarm extends Command
  case class AddTrigger(val listenFor: String) extends Command
  case class RemoveTrigger(val listenFor: String) extends Command
  
  trait DomainEvent
  case object AlarmArmed extends DomainEvent
  case object AlarmArming extends DomainEvent
  case object AlarmDisarmed extends DomainEvent
  case object AlarmDisarming extends DomainEvent
  case class TriggerAdded(val listenFor: String) extends DomainEvent
  case class TriggerRemoved(val listenFor: String) extends DomainEvent
  
  trait Data {
    val triggerList: Map[String, String]
    def addTrigger(trigger: String): Data
    def removeTrigger(trigger: String): Data
  }
  case object EmptyData extends Data {
    override val triggerList: Map[String, String] = Map.empty[String, String]
    override def addTrigger(trigger: String) = AlarmData(Map(trigger -> ""))
    override def removeTrigger(trigger: String) = EmptyData
    
  }
  case class AlarmData(triggerList: Map[String, String]) extends Data {
    override def addTrigger(trigger: String) = copy(triggerList + (trigger -> ""))
    override def removeTrigger(trigger: String) = copy(triggerList - trigger)
  }
  
  
  def props(id: String): Props = Props(new Alarm(id))
}

class Alarm(val persistenceId: String)(implicit val domainEventClassTag: ClassTag[Alarm.DomainEvent]) extends PersistentFSM[Alarm.State, Alarm.Data, Alarm.DomainEvent] {
    
  import Alarm._
//  import Thing.{Uninitialized, GetState, Die, DomainEvent}
  
  startWith(Disarmed, EmptyData)
 
  override def applyEvent(domainEvent: DomainEvent, currentData: Data): Data = {
    domainEvent match {
      case TriggerAdded(trig: String) => {
        val newData = currentData.addTrigger(trig)
        log.debug(s"trigger $trig added. current trigger list:\n${newData.triggerList}")
        newData
      }
      case TriggerRemoved(trig: String) => {
        val newData = currentData.removeTrigger(trig)
        log.debug(s"trigger $trig removed. current trigger list:\n${newData.triggerList}")
        newData
      }
      case AlarmArming => {
        armAlarm
        currentData
      }
      case AlarmDisarming => {
        disarmAlarm
        currentData
      }
      case AlarmArmed =>
        currentData
      case AlarmDisarmed =>
        currentData
    }
  }
  
  when (Disarmed) {
    case Event(GetState, _) =>
      stay replying Disarmed
    case Event(ArmAlarm, _) =>
      goto (Arming) applying AlarmArming andThen {
        case _ => armAlarm
      }
    case Event(AddTrigger(trig), _) =>
      stay applying TriggerAdded(trig) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: added trigger $trig")
      }
    case Event(RemoveTrigger(trig), _) =>
      stay applying TriggerRemoved(trig) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: removed trigger $trig")
      }
    case Event(Die, _) =>
      stop()
    case evt =>
      stay andThen {
        case _ => log.debug(s"alarm $persistenceId, received event $evt and not sure what to do. ignoring")
      }
  }
  
  when (Arming) {
    case Event(GetState, _) =>
      stay replying Arming
    case Event(AlarmArmed, _) =>
      goto (Armed) applying AlarmArmed andThen {
        case _ => log.debug(s"alarm $persistenceId: alarm armed")
      }
    case Event(AddTrigger(trig), _) =>
      stay applying TriggerAdded(trig) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: added trigger $trig")
      }
    case Event(RemoveTrigger(trig), _) =>
      stay applying TriggerRemoved(trig) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: removed trigger $trig")
      }
    case Event(Die, _) =>
      stop()
    case evt =>
      stay andThen {
        case _ => log.debug(s"alarm $persistenceId, received event $evt and not sure what to do. ignoring")
      }
  }
  
  when (Armed) {
    case Event(GetState, _) =>
      stay replying Armed
    case Event(DisarmAlarm, _) =>
      goto (Disarming) applying AlarmDisarming andThen {
        case _ => disarmAlarm
      }
    case Event(AddTrigger(trig), _) =>
      stay applying TriggerAdded(trig) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: added trigger $trig")
      }
    case Event(RemoveTrigger(trig), _) =>
      stay applying TriggerRemoved(trig) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: removed trigger $trig")
      }
    case Event(Die, _) =>
      stop()
    case evt =>
      stay andThen {
        case _ => log.debug(s"alarm $persistenceId, received event $evt and not sure what to do. ignoring")
      }
  }
  
  when (Disarming) {
    case Event(GetState, _) =>
      stay replying Disarming
    case Event(AlarmDisarmed, _) =>
      goto (Disarmed) applying AlarmDisarmed andThen {
        case _ => log.debug(s"alarm $persistenceId: alarm disarmed")
      }
    case Event(AddTrigger(trig), _) =>
      stay applying TriggerAdded(trig) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: added trigger $trig")
      }
    case Event(RemoveTrigger(trig), _) =>
      stay applying TriggerRemoved(trig) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: removed trigger $trig")
      }
    case Event(Die, _) =>
      stop()
    case evt =>
      stay andThen {
        case _ => log.debug(s"alarm $persistenceId, received event $evt and not sure what to do. ignoring")
      }
  }

  
//  val initial: Receive = {
//    case GetState => {
//      log.debug(s"alarm $persistenceId, state $state, received GetState")
//      respond()
//    }
//    case ArmAlarm => {
//      log.debug(s"door $persistenceId, state $state, received ArmAlarm")
//      persist(AlarmArming)(afterEventPersisted)
//      armAlarm 
//    }
//    case e: AddTrigger => {
//      log.debug(s"alarm $persistenceId, state $state, received AddTrigger of ${e.listenFor}")
//      persist(TriggerAdded(e.listenFor))(afterEventPersisted)
//    }
//    case e: RemoveTrigger => {
//      log.debug(s"alarm $persistenceId, state $state, received RemoveTrigger of ${e.listenFor}")
//      persist(TriggerRemoved(e.listenFor))(afterEventPersisted)
//    }
//    case Die => {
//      log.debug(s"door $persistenceId, got command to Die. Dying")
//      context stop self
//    }
//    case evt => {
////      log.debug(s"alarm $persistenceId, state $state, received event $evt and not sure what to do. ignoring")
//      respond()
//    }
//  }
  
//  val arming: Receive = {
//    case GetState => {
//      log.debug(s"alarm $persistenceId, state $state, received GetState")
//      respond()
//    }
//    case AlarmArmed => {
//      log.debug(s"door $persistenceId, state $state, received AlarmArmed")
//      persist(AlarmArmed)(afterEventPersisted)
//    }
//    case Die => {
//      log.debug(s"door $persistenceId, got command to Die. Dying")
//      context stop self
//    }
//    case evt => {
////      log.debug(s"alarm $persistenceId, state $state, received event $evt and not sure what to do. ignoring")
//      respond()
//    }
//  }
  
//  val armed: Receive = {
//    ???
//  }
  
//  val disarming: Receive = {
//    ???
//  }
    
//  val disarmed: Receive = {
//    ???
//  }

//  override val receiveCommand: Receive = initial

//  protected def restoreFromSnapshot(metadata: SnapshotMetadata, state: Thing.State): Unit = {
//    this.state = state
//    state match {
//      case Armed() => context become armed
//      case Arming() => context become arming
//      case Disarmed() => context become disarmed
//      case Disarming() => context become disarming
//      case _ => context become initial
//    }
//  }
  
//  def updateState(evt: DomainEvent): Unit = {
//    evt match {
//      case evt: TriggerAdded => {
//        triggerList += (evt.listenFor -> "")
//      }
//      case evt: TriggerRemoved => {
//        triggerList -=  evt.listenFor
//      }
//      case AlarmArmed => {
//        context become armed
//        state = Armed()
//      }
//      case AlarmArming => {
//        context become arming
//        state = Arming()
//      }
//      case AlarmDisarmed => {
//        context become disarmed
//        state = Disarmed()
//      }
//      case AlarmDisarming => {
//        context become disarming
//        state = Disarming()
//      }
//      case _ => {
//        context become initial
//        state = Uninitialized
//      }
//    log.debug(s"alarm $persistenceId, state updated to $state")
//    }
//  }
  
  private def armAlarm = {
    log.debug(s"alarm $persistenceId, arming alarm")
    self ! AlarmArmed
  }
  
  private def disarmAlarm = {
    log.debug(s"alarm $persistenceId, disarming alarm")
    self ! AlarmDisarmed   
  }
}