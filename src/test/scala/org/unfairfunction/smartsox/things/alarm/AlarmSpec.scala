package org.unfairfunction.smartsox.things.alarm

import akka.testkit._
import org.scalatest.{Matchers, FlatSpecLike}
import org.unfairfunction.smartsox.things.door.InMemoryCleanup
import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration._
import org.unfairfunction.smartsox.things.alarm.Alarm._
import org.unfairfunction.smartsox.things.door.Door
import org.unfairfunction.smartsox.things.door.Door._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import org.unfairfunction.smartsox.things.door.DoorsManager
import org.unfairfunction.smartsox.things.door.DoorsManager.{AddDoor, Close}

class AlarmSpec(system: ActorSystem)
  extends TestKit(system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with InMemoryCleanup {
  
  def this() = this(ActorSystem("AlarmSpec"))  
  implicit val timeout = Timeout(10 seconds)

  implicit val executionContext = system.dispatcher
  
  "Alarm" should "be created" in {
    val alarm = system.actorOf(Alarm.props, "alarmTestCreate")
    alarm should not be null
  }
  
  it should "return state Disarmed if not changed" in {
    val alarm = system.actorOf(Alarm.props, "alarmTestStateDisarmed")
    alarm should not be null
    alarm ! Alarm.GetState
    expectMsg(Disarmed)
  }
  
  it should "set trigger" in {
    val alarm = system.actorOf(Alarm.props, "alarmTestSetTrigger")
    val door = system.actorOf(Door.props, "doorTestSetTrigger")
    alarm ! Alarm.GetState
    expectMsg(Disarmed)
    alarm ! SetTrigger("doorTestAlarm", List(Opened))
    alarm ! GetData
    expectMsg(AlarmData("doorTestAlarm", List(Opened)))
  }
  
  it should "trigger an alarm on door opening" in {
    val alarm = system.actorOf(Alarm.props,"alarmTestTrigger")
    val door = system.actorOf(Door.props,"doorTestAlarm")
    val mediator = DistributedPubSub(system).mediator
    
    mediator ! Subscribe("alarmTestTrigger", self)
    expectMsgType[SubscribeAck]

    mediator ! Subscribe("doorTestAlarm", self)
    expectMsgType[SubscribeAck]
    
    door ! CloseDoor
    expectMsg(Closing)
    expectMsg(Closed)

    
    alarm ! Alarm.GetState
    expectMsg(Disarmed)
    alarm ! SetTrigger("doorTestAlarm", List(Opened))
    alarm ! ArmAlarm
    expectMsg(Arming)
    expectMsg(Armed)
    
    door ! OpenDoor
    
    expectMsg(Opening)
    expectMsg(Opened)
    expectMsg(Triggered)
  }
  
  
  it should "trigger an alarm on door opening, with DoorsManager" in {
    
    val alarm = system.actorOf(Alarm.props,"alarmTestTriggerWithDoorsManager")
    val dm = system.actorOf(DoorsManager.props("DoorsManager"), "DoorsManager")
    dm ! AddDoor("doorTestAlarmWithDoorsManager")
    expectMsg(Opened)
    val mediator = DistributedPubSub(system).mediator
    mediator ! Subscribe("doorTestAlarmWithDoorsManager", self)
    expectMsgType[SubscribeAck]
    mediator ! Subscribe("alarmTestTriggerWithDoorsManager", self)
    expectMsgType[SubscribeAck]

    dm ! DoorsManager.GetState("doorTestAlarmWithDoorsManager")
    expectMsg(Opened)
    dm ! Close("doorTestAlarmWithDoorsManager")
    expectMsg(Closing)
    expectMsg(Closed)


    
    alarm ! Alarm.GetState
    expectMsg(Disarmed)
    alarm ! SetTrigger("doorTestAlarmWithDoorsManager", List(Opened))
    alarm ! ArmAlarm
    expectMsg(Arming)
    expectMsg(Armed)
    
    dm ! DoorsManager.Open("doorTestAlarmWithDoorsManager")
    
    expectMsg(Opening)
    expectMsg(Opened)
    expectMsg(Triggered)
  }
}