package org.unfairfunction.smartsox.things.door

import org.unfairfunction.smartsox.actors.{ThingsManager, ThingActor}
import org.unfairfunction.smartsox.actors.ThingActor.GetThing
import org.unfairfunction.smartsox.things.door.DoorActor.{Die, OpenDoor, CloseDoor}
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.actor.{Props, Actor, ActorRef, ActorLogging}
import scala.concurrent.duration.DurationInt
import scala.util.{Success, Failure}
//import org.unfairfunction.smartsox.actors.Thing.{GetState, Die}
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.JsonMethods._
import org.json4s.ext.JodaTimeSerializers
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import akka.stream.ActorMaterializer
//import akka.http.scaladsl.model.Uri.Path.Segment._
//import akka.http.scaladsl.model.StatusCodes
//import akka.http.scaladsl.model.HttpResponse


object DoorsManager extends DoorsManagerApi {
  import ThingsManager._
  import Json4sSupport._
 
  implicit val mat = ActorMaterializer
//  implicit val serialization = native.Serialization
//  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all
//  implicit val formats = serialization.formats(NoTypeHints)
  implicit def json4sFormats = DefaultFormats.withCompanions(classOf[Door] -> Door) ++ JodaTimeSerializers.all

//  trait Command extends 
  case class AddDoor(doorId: String) extends Command
  case class RemoveDoor(doorId: String) extends Command
  case class Open(doorId: String) extends Command
  case class Close(doorId: String) extends Command
  
  def props(id: String) = Props(new DoorsManager)
  
  implicit val timeout: akka.util.Timeout = 10 second
  
//  def routes(doorsManager: ActorRef) = {
//    pathEndOrSingleSlash {
//      complete("doors")
//    } ~
//    path ("get" / Segment) { id =>
//      get {
//
//        onComplete(doorsManager ? ThingsManager.Get(id)) {
//          case Success(d: Door) => {
//            println(s"door ${d} found")
//            complete(200, d)
//          }
//          case Success(_) => {
//            println(s"door $id not found")
//            complete(404, s"door $id not found")
//          }
//          case Failure(reason) => complete(500, reason)
//        }
//      }
//    } ~
//    path("exists" / Segment) { s =>
//      get {
//        onComplete(doorsManager ? ThingsManager.Exists(s)) {
//          case Success(status) => {
//            status match {
//              case None => complete(404, s"door $s not found")
//              case _ => complete(200, s"door $s found")
//            }
//          }
//          case Failure(reason) => complete(s"could not create door. reason: $reason")
//        }        
//      }      
//    } ~
//    path("state" / Segment){ s =>
//      println(s"got state")
//      get {
////        entity(as[ThingName]) { doorName =>
//          onComplete(doorsManager ? ThingsManager.GetState(s)) {
//            case Success(status) => {
//              status match {
//                case None => complete(404, s"door $s not found")
//                case _ => complete(s"door ${s} state: $status")
//              }
//            }
//            case Failure(reason) => complete(s"could not get door state. reason: $reason")
////            case Failure(reason) => reject(s"could not create door. reason: $reason")
////          }                    
//        }
//      }
//    } ~
//    path("create"){
//      println(s"got create")
//      post {
//        entity(as[Door]) { door =>
//          println(s"create door $door")
//          onComplete(doorsManager ? ThingsManager.Create(door)) {
//            case Success(status) => {
//              status match {
//                case AlreadyExists(id) => {
//                  println(s"door $id already exists")
//                  complete(400, s"door $id already exists")
//                }
//                case _ => {
//                  println(s"door ${door.name} not found. creating")
//                  complete(201, s"door created. state: $status")
//                }
//              }
//            }
//            case Failure(reason) => complete(s"could not create door. reason: $reason")
//          }          
//        }
//      }
//    }
//  }
}

class DoorsManager extends ThingsManager {
  
  
import ThingsManager._
import DoorsManager._
import akka.util.Timeout
  
//class DoorsManager extends Actor with ActorLogging {
  
  log.debug(s"DoorsManager ${context.self.path.name}  created")
  
  implicit val executionContext = context.system.dispatcher
  
//  def receive =  {
  override def processCommand =  {
    case ThingsManager.Create(door) => {
      find(door.name) match {
        case None => create(door) forward GetState
        case Some(_) => sender ! AlreadyExists(door.name)
      }
    }
    case ThingsManager.Exists(id) => {
      sender ! find(id) 
    }
    case ThingsManager.Get(id) => {
      find(id) match {
        case None => {
          println(s"Get($id) did not find anything")
          sender ! NotFound(id)
        }
        case Some(actorref) => {
          val mySender = sender
          ((actorref ? GetThing).mapTo[Door])
          .onComplete { 
            case Success(door) => mySender ! door
            case Failure(reason) => mySender ! NotFound(id)
          }
        }
      }
    }
    case GetState(id) => sendToChild(id, ThingActor.GetState)
    case DoorsManager.AddDoor(id) => sendToChild(id, ThingActor.GetState)
    case DoorsManager.RemoveDoor(id) => sendToChild(id, DoorActor.Die)
    case DoorsManager.Open(id) => sendToChild(id, DoorActor.OpenDoor)
    case DoorsManager.Close(id) => sendToChild(id, DoorActor.CloseDoor)    
  }
  
  def sendToChild(thingId: String, command: ThingActor.Command) = {
    find(thingId) match {
      case Some(child) => child forward command
      case None => sender ! None
    }
//    val child = findOrCreate(thingId) 
//    child forward command    
  }
  
  override protected def find(id: String): Option[ActorRef] =
    context child id
  
//  override protected def findOrCreate(id: String): ActorRef ={
//    find(id) match {
//      case Some(ref) => {
////        println(s"found door with id $id")
//        ref
//      }
//      case None => {
////        println(s"door $id was not found. creating")
//        create(id)
//      }
//    }
//  }

//  protected def create(id: String): ActorRef = {
//    val nextThing = context.actorOf(Door.props, id)
////    println(s"father: $self")
////    println(s"daughter: $nextThing")
//    context watch nextThing
//    nextThing
//  }

    
  def props(id: String) = DoorsManager.props(id)
  
  protected def thingProps(door: org.unfairfunction.smartsox.actors.Thing) = {
    assert (door.isInstanceOf[Door])
    DoorActor.props(door.asInstanceOf[Door])
  }
}