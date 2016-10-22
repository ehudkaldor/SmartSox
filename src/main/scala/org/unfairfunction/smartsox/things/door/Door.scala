package org.unfairfunction.smartsox.things.door

import org.unfairfunction.smartsox.actors.Thing
import akka.actor.Props
import akka.persistence.SnapshotMetadata

object Door {
  import Thing._
  
  def props(id: String): Props = Props(new Door(id))
}

class Door(val persistenceId: String) extends Thing {
  import Door._
  
  protected def restoreFromSnapshot(metadata: SnapshotMetadata,state: Thing.State): Unit = {
    this.state = state
//    state match {
//      case Uninitialized => context become initial
//      case Removed => context become removed
//      case _: User => context become created
//    }
  }
  
  def updateState(evt: org.unfairfunction.smartsox.actors.Thing.Event): Unit = ???
  
  override val receiveCommand: Receive = ???
}