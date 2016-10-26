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

class ChatClientSpec(system: ActorSystem)
  extends TestKit(system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("ChatClientSpec"))  
  implicit val timeout = Timeout(10 seconds)

  implicit val executionContext = system.dispatcher
  
  override def afterAll = {
    system.terminate()
  }
  
  "Door" should "should be created correctly" in {
    val door = system.actorOf(Door.props("testdoor"))
    door should not be null
  }
  
  it should "return state Uninitialized if not changed" in {
    val door = system.actorOf(Door.props("testdoor"))
    door ! GetState
    expectMsg(Uninitialized)
  }
  
  it should "return state Opened after sent message to open door" in {
    val door = system.actorOf(Door.props("testdoor"))
    door ! OpenDoor
    door ! GetState
    expectMsg(Opened)
  }
}