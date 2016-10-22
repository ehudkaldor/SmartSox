package org.unfairfunction.smartsox.actors

import java.util.Date
import akka.persistence.{PersistentActor, SnapshotOffer, SnapshotMetadata}
import akka.actor.{Actor, ActorLogging}
import org.unfairfunction.smartsox.util.Acknowledge

object Thing {
  trait State
  case object Uninitialized extends State
  case object Removed extends State

  trait Event

  trait Command
  case object Remove extends Command
  case object GetState extends Command

  case object KillThing extends Command

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
    sender() ! state
    context.parent ! Acknowledge(persistenceId)
  }

  private def publish(event: Event) = context.system.eventStream.publish(event)
  
  override val receiveRecover: Receive = {
    case evt: Event =>
      eventsSinceLastSnapshot += 1
      updateState(evt)
    case SnapshotOffer(metadata, state: State) =>
      restoreFromSnapshot(metadata, state)
      log.debug("recovering aggregate from snapshot")
  }

  protected def restoreFromSnapshot(metadata: SnapshotMetadata, state: State)


}

//trait ThingMessage {
//  val id: Long
//  val timestamp: Date
//}
//  
//trait Trigger extends ThingMessage {
//    
//}
//  
//trait Update extends ThingMessage {
//    
//}
