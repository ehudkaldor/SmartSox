package org.unfairfunction.smartsox.things.door

import akka.actor.{ActorSystem, PoisonPill, Terminated}
import scala.concurrent.duration._
import akka.util.Timeout
import akka.testkit._
import org.unfairfunction.smartsox.things.door.DoorActor._
import org.scalatest.{Matchers, FlatSpecLike}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import org.unfairfunction.smartsox.actors.ThingActor.GetState

class DoorSpec(system: ActorSystem)
  extends TestKit(system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with InMemoryCleanup {

  def this() = this(ActorSystem("DoorSpec"))  
  implicit val timeout = Timeout(10 seconds)

  implicit val executionContext = system.dispatcher
  
//  override def afterAll = {
//    system.terminate()
//  }
  
  "Door" should "should be created correctly" in {
    val door = Door("testdoorCreate")
    val doorActor = system.actorOf(DoorActor.props(door), door.name)
    doorActor should not be null
  }
  
  it should "return state Opened if not changed" in {
    val door = Door("testdoorUninitialized")
    val doorActor = system.actorOf(DoorActor.props(door), door.name)
    doorActor should not be null
    doorActor ! GetState
    expectMsg(Opened)
    doorActor ! Die
  }
  
//  it should "return state Opening after sending message to open door, and Opened on GetState afterwards" in {
//    val door = system.actorOf(Door.props("testdoorOpen"))
//    door ! OpenDoor
//    expectMsg(Opening)
//    Thread.sleep(1000)
//    door ! GetState
//    expectMsg(Opened)
//  }
  
  it should "return state Closing after sending message to close door, and Closed on GetState afterwards" in {

    val door = Door("testdoorClose")
    val doorActor = system.actorOf(DoorActor.props(door), door.name)
    val mediator = DistributedPubSub(system).mediator
    mediator ! Subscribe("testdoorClose", self)
    expectMsgType[SubscribeAck]
    
    doorActor ! CloseDoor
    expectMsg(Closing)
    Thread.sleep(1000)
//    door ! GetState
    expectMsg(Closed)
    doorActor ! Die
  }
  
  it should "only lock from Closed state" in {
    val door = Door("testdoorLockFromClosedOnly")
    val doorActor = system.actorOf(DoorActor.props(door), door.name)
    val mediator = DistributedPubSub(system).mediator
    
    mediator ! Subscribe("testdoorLockFromClosedOnly", self)
    expectMsgType[SubscribeAck]
//    door ! GetState
//    expectMsg(Closed)
    doorActor ! LockDoor
//    door ! GetState
//    expectMsg(Uninitialized)
//    door ! OpenDoor
//    expectMsg(Opening)
//    Thread.sleep(1000)
//    door ! GetState
    expectMsg(Opened)        
//    door ! LockDoor
//    door ! GetState
//    expectMsg(Opened)
    doorActor ! CloseDoor
    expectMsg(Closing)
//    Thread.sleep(1000)
//    door ! GetState
    expectMsg(Closed)        
    doorActor ! LockDoor
    expectMsg(Locking)
    Thread.sleep(1000)
//    door ! GetState
    expectMsg(Locked)  
    doorActor ! UnlockDoor
    expectMsg(Unlocking)
//    Thread.sleep(1000)
//    door ! GetState
    expectMsg(Closed)
  }
  
  it should "return state Locking after sending message to lock door, and Locked on GetState afterwords" in {
    val door= Door("testdoorLock")
    val doorActor = system.actorOf(DoorActor.props(door), door.name)
    val mediator = DistributedPubSub(system).mediator
    mediator ! Subscribe("testdoorLock", self)
    expectMsgType[SubscribeAck]
    doorActor ! CloseDoor
    expectMsg(Closing)
    Thread.sleep(1000)
//    door ! GetState
    expectMsg(Closed)        
    doorActor ! LockDoor
    expectMsg(Locking)
//    Thread.sleep(1000)
//    door ! GetState
    expectMsg(Locked)    
  }
  
  it should "stay locked when told to lock, even on sending open and close messages, and return state Closed when told to unlock" in {
    val door= Door("testdoorStayLocked")
    val doorActor = system.actorOf(DoorActor.props(door), door.name)
    val mediator = DistributedPubSub(system).mediator
    mediator ! Subscribe("testdoorStayLocked", self)
    expectMsgType[SubscribeAck]
    doorActor ! CloseDoor
    expectMsg(Closing)
    Thread.sleep(1000)
//    door ! GetState
    expectMsg(Closed)        
    doorActor ! LockDoor
    expectMsg(Locking)
    Thread.sleep(1000)
//    door ! GetState
    expectMsg(Locked)    
    doorActor ! OpenDoor
//    door ! GetState
    expectMsg(Locked)
    doorActor ! CloseDoor
//    door ! GetState
    expectMsg(Locked)
    doorActor ! UnlockDoor
//    Thread.sleep(3000)
    expectMsg(Unlocking)
    Thread.sleep(1000)
//    door ! GetState
    expectMsg(Closed)    
  }
  
  it should "die after instructed to die" in {
    val door= Door("testdoorDie")
    val doorActor = system.actorOf(DoorActor.props(door), door.name)
    watch(doorActor)
    doorActor ! Die
    expectMsg(Terminated(doorActor)(true, true))
  }
}