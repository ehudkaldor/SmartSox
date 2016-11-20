//package org.unfairfunction.smartsox.actors
//
//import akka.actor.{Actor, ActorLogging}
//import akka.cluster.pubsub.DistributedPubSub
//import java.util.Date
//import akka.persistence.{PersistentActor, SnapshotOffer, SnapshotMetadata}
//import org.unfairfunction.smartsox.util.Acknowledge
//
//
//trait PublisherTrait extends Actor with ActorLogging with PersistentActor {
//  
//  import PublisherTrait._
//  
//  val mediator = DistributedPubSub(context.system).mediator
//  val topicList: List[String]
//  val thing: Thing
//   
//  protected var state: State = Uninitialized
//   
//  override val persistenceId: String
//  
//  private var eventsSinceLastSnapshot = 0
//   
//  def publish(msg: PublishMessage) {
//    mediator ! msg
//  }
//   
//  def updateState(event: Event): Unit
// 
//  override val receiveRecover: Receive = {
//    case evt: Event =>
//      eventsSinceLastSnapshot += 1
//      updateState(evt)
//    case SnapshotOffer(metadata, state: State) =>
//      restoreFromSnapshot(metadata, state)
//      log.debug("recovering aggregate from snapshot")
//  }
//
//  protected def restoreFromSnapshot(metadata: SnapshotMetadata, state: State)
//  
//  protected def afterEventPersisted(evt: Event): Unit = {
//    eventsSinceLastSnapshot += 1
//    if (eventsSinceLastSnapshot >= eventsPerSnapshot) {
//      log.debug("{} events reached, saving snapshot", eventsPerSnapshot)
//      saveSnapshot(state)
//      eventsSinceLastSnapshot = 0
//    }
//    updateAndRespond(evt)
//    publish(evt)
//  }
//
//  private def updateAndRespond(evt: Event): Unit = {
//    updateState(evt)
//    respond()
//  }
//
//  protected def respond(): Unit = {
//    sender() ! state
//    context.parent ! Acknowledge(persistenceId)
//  }
//
//  private def publish(event: Event) =
//    context.system.eventStream.publish(event)
//
//
//
//}
//
//object PublisherTrait {
//  trait State
//  case object Uninitialized extends State
//  
//  trait Event
//
//  trait Command
//  
//  val eventsPerSnapshot = 10
//}
//
//abstract class PublishMessage(id: Long, timestamp: Date, flowId: Int, seqId: Int = 0)
//
