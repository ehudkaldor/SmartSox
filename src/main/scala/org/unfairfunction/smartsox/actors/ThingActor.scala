package org.unfairfunction.smartsox.actors

import java.util.Date
//import akka.persistence.{PersistentActor, SnapshotOffer, SnapshotMetadata}
import akka.actor.{Actor, ActorLogging}
import org.unfairfunction.smartsox.util.Acknowledge
import java.util.Calendar
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.{FSMState, Reason}
import scala.reflect.{ClassTag, classTag}

object ThingActor {
  trait State extends FSMState
  case object Uninitialized extends State {
    override def identifier: String = "uninitialized"
  }
  case object Retired extends State {
    override def identifier: String = "retired"
  }
  
  trait Data {
    def empty: Data = EmptyData
  }
  
  case object EmptyData extends Data

  
  trait DomainEvent {
    protected val createTime: Calendar = Calendar.getInstance
  }
  case object ThingRetired extends DomainEvent
  trait CommandFailed extends DomainEvent

  trait Command {
    protected val createTime: Calendar = Calendar.getInstance
  }

  case object GetState extends Command
  case object GetThing extends Command
  case object Die extends Command
  case object Retire extends Command
  case object Unretire extends Command

//  case object KillThing extends Command

  val eventsPerSnapshot = 10  
}

//trait Thing extends Actor with PersistentActor with ActorLogging{
trait ThingActor extends PersistentFSM[ThingActor.State, ThingActor.Data, ThingActor.DomainEvent] with ActorLogging{
  import ThingActor._
  import akka.cluster.pubsub.DistributedPubSub
  import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Publish}
  
  val mediator = DistributedPubSub(context.system).mediator
  
  val myThing: Thing
  
  override val persistenceId: String
  
	override def domainEventClassTag: ClassTag[DomainEvent] = classTag[DomainEvent]

  startWith(Uninitialized, EmptyData)
	
	def applyEvent(domainEvent: DomainEvent, currentData: Data): Data
  
  when (Uninitialized) {
    case Event(GetState, data) =>
      stay replying data
    case Event(Retire, _) =>
      goto (Retired) applying ThingRetired 
  }

  private var eventsSinceLastSnapshot = 0

//  def updateState(evt: DomainEvent): Unit
  
  mediator ! Subscribe("general", self)
 
  protected def afterEventPersisted(evt: DomainEvent): Unit = {
    eventsSinceLastSnapshot += 1
    if (eventsSinceLastSnapshot >= eventsPerSnapshot) {
      log.debug(s"$eventsPerSnapshot events reached, saving snapshot")
      saveStateSnapshot()
      eventsSinceLastSnapshot = 0
    }
//    updateAndRespond(evt)
    publish(evt)
  }

//  private def updateAndRespond(evt: DomainEvent): Unit = {
//    updateState(evt)
//    respond()
//  }

  protected def respond(): Unit = {
//    log.debug(s"sender: $sender()")
//    sender() ! state
    context.parent ! Acknowledge(persistenceId)
  }

  private def publish(event: DomainEvent) = {
    context.system.eventStream.publish(event)
    mediator ! Publish(persistenceId, event)
  }
  
//  override val receiveRecover: Receive = {
//    case evt: DomainEvent =>
//      log.debug(s"recovering event $evt from snapshot")
//      eventsSinceLastSnapshot += 1
//      updateState(evt)
//    case SnapshotOffer(metadata, state: State) =>
//      log.debug("recovering aggregate from snapshot")
//      restoreFromSnapshot(metadata, state)
//  }

//  protected def restoreFromSnapshot(metadata: SnapshotMetadata, state: State)

}