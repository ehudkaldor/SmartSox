package org.unfairfunction.smartsox.things.alarm

import akka.actor.Props
import akka.persistence.SnapshotMetadata
import scala.concurrent.duration.DurationInt
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import scala.reflect.ClassTag
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish,Subscribe, Unsubscribe}

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
  case object GetData extends Command
  case object Die extends Command
  case object ArmAlarm extends Command
  case object DisarmAlarm extends Command
  case class SetTrigger(val listenTo: String, listenFor: List[Any]) extends Command
  case class UnsetTrigger(val listenTo: String) extends Command
  
  trait DomainEvent
  case object AlarmArmed extends DomainEvent
  case object AlarmArming extends DomainEvent
  case object AlarmDisarmed extends DomainEvent
  case object AlarmDisarming extends DomainEvent
  case class TriggerSet(val listenTo: String, listenFor: List[Any]) extends DomainEvent
  case class TriggerUnset(val listenTo: String) extends DomainEvent
  
  trait Data {
//    val triggerList: Map[String, List[Any]]
    val listenTo: String = ""
    val listenFor: List[Any] = List.empty[Any]
    def setTrigger(listenTo: String, listenFor: List[Any]): Data
    def unsetTrigger: Data
  }
  case object EmptyData extends Data {
//    override val triggerList: Map[String, List[Any]] = Map.empty[String, List[Any]]
    override def setTrigger(listenTo: String, listenFor: List[Any]) = AlarmData(listenTo, listenFor)
    override def unsetTrigger = EmptyData
    
  }
  case class AlarmData(override val listenTo: String, override val listenFor: List[Any]) extends Data {
    override def setTrigger(listenTo: String, listenFor: List[Any]) = AlarmData(listenTo, listenFor)
    override def unsetTrigger: Data = EmptyData
  }
  
  
//  def props(id: String): Props = Props(new Alarm(id))
  def props: Props = Props(new Alarm())
}

//class Alarm(val persistenceId: String)(implicit val domainEventClassTag: ClassTag[Alarm.DomainEvent]) extends PersistentFSM[Alarm.State, Alarm.Data, Alarm.DomainEvent] {
class Alarm(implicit val domainEventClassTag: ClassTag[Alarm.DomainEvent]) extends PersistentFSM[Alarm.State, Alarm.Data, Alarm.DomainEvent] {
    
  import Alarm._
//  import Thing.{Uninitialized, GetState, Die, DomainEvent}
  
  val persistenceId: String = self.path.name
  
  val mediator = DistributedPubSub(context.system).mediator
  
  log.debug(s"alarm $persistenceId created")
  
  startWith(Disarmed, EmptyData)
 
  override def applyEvent(domainEvent: DomainEvent, currentData: Data): Data = {
    domainEvent match {
      case TriggerSet(listenTo: String, listenFor: List[Any]) => {
        val newData = currentData.setTrigger(listenTo, listenFor)
        mediator ! Subscribe(listenTo, self)
        log.debug(s"trigger $listenTo set, listening for $listenFor")
        newData
      }
      case TriggerUnset(listenTo: String) => {
        val newData = currentData.unsetTrigger
        mediator ! Unsubscribe(listenTo, self)
        log.debug(s"trigger $listenTo unset")
        newData
      }
      case AlarmArming => {
//        armAlarm
        currentData
      }
      case AlarmDisarming => {
//        disarmAlarm
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
    case Event(GetData, data) =>
      stay replying data
    case Event(ArmAlarm, _) =>
      goto (Arming) applying AlarmArming andThen {
        case _ => {
          mediator ! Publish(persistenceId, Arming)
          armAlarm
        }
      }
    case Event(SetTrigger(listenTo, listenFor), _) =>
      stay applying TriggerSet(listenTo, listenFor) andThen {
        case _ => log.debug(s"alarm $persistenceId: setting trigger $listenTo, listening for $listenFor")
      }
    case Event(UnsetTrigger(listenTo), _) =>
      stay applying TriggerUnset(listenTo) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: unsetting trigger $listenTo")
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
        case _ => {
          mediator ! Publish(persistenceId, Armed)
          log.debug(s"alarm $persistenceId: alarm armed")
        }
      }
    case Event(SetTrigger(listenTo, listenFor), _) =>
      stay applying TriggerSet(listenTo, listenFor) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: setting trigger $listenTo, listening for $listenFor")
      }
    case Event(UnsetTrigger(trig), _) =>
      stay applying TriggerUnset(trig) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: unsetting trigger $trig")
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
        case _ => {
          mediator ! Publish(persistenceId, Disarming)
          disarmAlarm
        }
      }
    case Event(SetTrigger(listenTo, listenFor), _) =>
      stay applying TriggerSet(listenTo, listenFor) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: setting trigger $listenTo, listening to $listenFor")
      }
    case Event(UnsetTrigger(trig), _) =>
      stay applying TriggerUnset(trig) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: unsetting trigger $trig")
      }
    case Event(Die, _) =>
      stop()
    case evt =>
      log.debug(s"got event ${evt}, event in list: ${evt.stateData.listenFor.contains(evt.event)}, sender is listenTo: ${sender.path.name == evt.stateData.listenTo}")
      if (evt.stateData.listenFor.contains(evt.event)) {
            log.debug(s"**********  ALARM TRIGGERED by ${evt.stateData.listenTo} for event ${evt.event}   ************************ ")
            mediator ! Publish(persistenceId, Triggered)
      }
      evt.event match {
        case e if evt.stateData.listenFor.contains(e) => stay andThen {
          case _ => {
            log.debug(s"**********  ALARM TRIGGERED by ${evt.stateData.listenTo} for event ${evt.event}, sender: $sender ************************ ")
            mediator ! Publish(persistenceId, Triggered)
          }
        }
        case _ => stay andThen {
          case _ => log.debug(s"alarm $persistenceId, received event $evt and not sure what to do. ignoring")
        }
      }
  }
  
  when (Disarming) {
    case Event(GetState, _) =>
      stay replying Disarming
    case Event(AlarmDisarmed, _) =>
      goto (Disarmed) applying AlarmDisarmed andThen {
        case _ => {
          mediator ! Publish(persistenceId, Disarmed)
          log.debug(s"alarm $persistenceId: alarm disarmed")
        }
      }
    case Event(SetTrigger(listenTo, listenFor), _) =>
      stay applying TriggerSet(listenTo, listenFor) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: setting trigger $listenTo, listening to $listenFor")
      }
    case Event(UnsetTrigger(trig), _) =>
      stay applying TriggerUnset(trig) forMax(1 seconds) andThen {
        case _ => log.debug(s"alarm $persistenceId: unsetting trigger $trig")
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