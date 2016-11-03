package org.unfairfunction.smartsox.actors

import java.util.Date
import akka.persistence.{PersistentActor, SnapshotOffer, SnapshotMetadata}
import akka.actor.{Actor, ActorLogging}
import org.unfairfunction.smartsox.util.Acknowledge
import java.util.Calendar

object Thing {
  trait State 
  case object Uninitialized extends State
  case object Retired extends State

  trait Command {
    protected val createTime: Calendar = Calendar.getInstance
  }
  
  trait Event {
    protected val createTime: Calendar = Calendar.getInstance
  }

  trait CommandFailed extends Event

//  trait Command
//  case object Remove extends Command
  case object GetState extends Command
  case object Die extends Command
  case object Retire extends Command
  case object Unretire extends Command

//  case object KillThing extends Command

  val eventsPerSnapshot = 10  
}

trait Thing extends Actor with PersistentActor with ActorLogging{
  import Thing._
  
  override val persistenceId: String

  protected var state: State = Uninitialized

  private var eventsSinceLastSnapshot = 0

  def updateState(evt: Event): Unit
 
  protected def afterEventPersisted(evt: Event): Unit = {
    eventsSinceLastSnapshot += 1
    if (eventsSinceLastSnapshot >= eventsPerSnapshot) {
      log.debug(s"$eventsPerSnapshot events reached, saving snapshot")
      saveSnapshot(state)
      eventsSinceLastSnapshot = 0
    }
    updateAndRespond(evt)
    publish(evt)
  }

  private def updateAndRespond(evt: Event): Unit = {
    updateState(evt)
    respond()
  }

  protected def respond(): Unit = {
//    log.debug(s"sender: $sender()")
    sender() ! state
    context.parent ! Acknowledge(persistenceId)
  }

  private def publish(event: Event) = context.system.eventStream.publish(event)
  
  override val receiveRecover: Receive = {
    case evt: Event =>
      log.debug(s"recovering event $evt from snapshot")
      eventsSinceLastSnapshot += 1
      updateState(evt)
    case SnapshotOffer(metadata, state: State) =>
      log.debug("recovering aggregate from snapshot")
      restoreFromSnapshot(metadata, state)
  }

  protected def restoreFromSnapshot(metadata: SnapshotMetadata, state: State)

}