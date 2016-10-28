package org.unfairfunction.smartsox.things.door

import org.scalatest.BeforeAndAfterAll
import akka.actor.ActorSystem
import scala.concurrent.duration._
import akka.util.Timeout
import akka.testkit._
import org.unfairfunction.smartsox.actors.Thing.GetState
import org.scalatest.Matchers
import org.scalatest.FlatSpecLike
import org.unfairfunction.smartsox.actors.Thing
import org.unfairfunction.smartsox.actors.Thing.Uninitialized
import org.unfairfunction.smartsox.things.door.Door.OpenDoor
import org.unfairfunction.smartsox.things.door.Door.Opened
import org.scalatest.BeforeAndAfterEach
import org.unfairfunction.smartsox.things.door.Door.Opening
import org.unfairfunction.smartsox.things.door.Door.CloseDoor
import org.unfairfunction.smartsox.things.door.Door.Closing
import org.unfairfunction.smartsox.things.door.Door.Closed

class DoorSpec(system: ActorSystem)
  extends TestKit(system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll
  with InMemoryCleanup {

  def this() = this(ActorSystem("DoorSpec"))  
  implicit val timeout = Timeout(10 seconds)

  implicit val executionContext = system.dispatcher
  
  override def afterAll = {
    system.terminate()
  }
  
  "Door" should "should be created correctly" in {
    val door = system.actorOf(Door.props("testdoor1"))
    door should not be null
  }
  
  it should "return state Uninitialized if not changed" in {
    val door = system.actorOf(Door.props("testdoor2"))
    door ! GetState
    expectMsg(Uninitialized)
  }
  
  it should "return state Opening after sent message to open door, and Opened afterwards" in {
    val door = system.actorOf(Door.props("testdoor3"))
    door ! OpenDoor
    expectMsg(Opening)
    Thread.sleep(1000)
    door ! GetState
    expectMsg(Opened)
  }
  
  it should "return state Closing after sent message to close door, and Closed afterwards" in {
    val door = system.actorOf(Door.props("testdoor4"))
    door ! CloseDoor
    expectMsg(Closing)
    Thread.sleep(1000)
    door ! GetState
    expectMsg(Closed)
  }
}