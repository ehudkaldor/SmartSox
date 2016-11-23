package org.unfairfunction.smartsox.things.door

import scala.concurrent.duration._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.util.Timeout
import org.unfairfunction.smartsox.things.door.DoorsManager.{AddDoor, RemoveDoor, Open, Close, GetState}
import org.unfairfunction.smartsox.things.door.Door.{DoorOpening, Opening, Opened, Closing, Closed}
import akka.actor.Terminated

class DoorsManagerSpec(system: ActorSystem)
  extends TestKit(system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with InMemoryCleanup {

  def this() = this(ActorSystem("DoorsManagerSpec"))  
  implicit val timeout = Timeout(10 seconds)

  implicit val executionContext = system.dispatcher

  "Doors Manager" should "be created" in {
    val dm = system.actorOf(DoorsManager.props("DoorsManager1"), "DoorsManager1")
    dm should not be null
  }
  
  it should "create a new door" in {
    val dm = system.actorOf(DoorsManager.props("DoorsManager2"), "DoorsManager2")
    dm ! AddDoor("DoorsManagerTestDoor2")
    expectMsg(Opened)
    
  }
  
  it should "open a door and then close it" in {
    val dm = system.actorOf(DoorsManager.props("DoorsManager3"), "DoorsManager3")
    dm ! AddDoor("DoorsManagerTestDoor3")
    expectMsg(Opened)
//    dm ! Open("DoorsManagerTestDoor3")
//    expectMsg(Opening)
    dm ! GetState("DoorsManagerTestDoor3")
    expectMsg(Opened)
    dm ! Close("DoorsManagerTestDoor3")
    expectMsg(Closing)
    Thread.sleep(1000)
    dm ! GetState("DoorsManagerTestDoor3")
    expectMsg(Closed)
  }
  
  it should "destroy a door" in {
    val dm = system.actorOf(DoorsManager.props("DoorsManager4"),"DoorsManager4")
    watch(dm)
    dm ! AddDoor("DoorsManagerTestDoor4")
    expectMsg(Opened)
    dm ! Close("DoorsManagerTestDoor4")
    expectMsg(Closing)
    Thread.sleep(1000)
    dm ! GetState("DoorsManagerTestDoor4")
    expectMsg(Closed)

    dm ! RemoveDoor("DoorsManagerTestDoor4")
    Thread.sleep(10000)
    dm ! AddDoor("DoorsManagerTestDoor4")
    expectMsg(Opened)
  }
  
}