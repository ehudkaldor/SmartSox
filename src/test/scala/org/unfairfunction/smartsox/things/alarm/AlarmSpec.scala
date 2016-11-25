package org.unfairfunction.smartsox.things.alarm

import akka.testkit._
import org.scalatest.{Matchers, FlatSpecLike}
import org.unfairfunction.smartsox.things.door.InMemoryCleanup
import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration._
import org.unfairfunction.smartsox.things.alarm.Alarm.{GetState, Disarmed, SetTrigger, GetData, AlarmData}
import org.unfairfunction.smartsox.things.door.Door
import org.unfairfunction.smartsox.things.door.Door.Opened

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
    val alarm = system.actorOf(Alarm.props("alarmTestCreate"))
    alarm should not be null
  }
  
  it should "return state Disarmed if not changed" in {
    val alarm = system.actorOf(Alarm.props("alarmTestStateDisarmed"))
    alarm should not be null
    alarm ! GetState
    expectMsg(Disarmed)
  }
  
  it should "set trigger" in {
    val alarm = system.actorOf(Alarm.props("alarmTestSetTrigger"))
    val door = system.actorOf(Door.props("doorTestAlarm"))
    alarm ! GetState
    expectMsg(Disarmed)
    alarm ! SetTrigger("doorTestAlarm", List(Opened))
    alarm ! GetData
    expectMsg(AlarmData("doorTestAlarm", List(Opened)))
  }
  
  it should "unset trigger" in {
    
  }
  
  

}