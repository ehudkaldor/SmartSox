package org.unfairfunction.smartsox.things.door

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.Directives._
import org.unfairfunction.smartsox.actors.ThingsManager
import org.unfairfunction.smartsox.actors.ThingsManager._
import org.unfairfunction.smartsox.util.JsonSupport
import scala.util.{Success, Failure}
import akka.util.Timeout


trait DoorsManagerApi extends JsonSupport {
  
  implicit val timeout: Timeout
  
  
  def routes(doorsManager: ActorRef) = {
    pathEndOrSingleSlash {
      complete("doors")
    } ~
    path ("get" / Segment) { id =>
      get {

        onComplete(doorsManager ? ThingsManager.Get(id)) {
          case Success(d: Door) => {
            println(s"door ${d} found")
            complete(200, d)
          }
          case Success(_) => {
            println(s"door $id not found")
            complete(404, s"door $id not found")
          }
          case Failure(reason) => complete(500, reason)
        }
      }
    } ~
    path("exists" / Segment) { s =>
      get {
        onComplete(doorsManager ? ThingsManager.Exists(s)) {
          case Success(status) => {
            status match {
              case None => complete(404, s"door $s not found")
              case _ => complete(200, s"door $s found")
            }
          }
          case Failure(reason) => complete(s"could not create door. reason: $reason")
        }        
      }      
    } ~
    path("state" / Segment){ s =>
      println(s"got state")
      get {
//        entity(as[ThingName]) { doorName =>
          onComplete(doorsManager ? ThingsManager.GetState(s)) {
            case Success(status) => {
              status match {
                case None => complete(404, s"door $s not found")
                case _ => complete(s"door ${s} state: $status")
              }
            }
            case Failure(reason) => complete(s"could not get door state. reason: $reason")
//            case Failure(reason) => reject(s"could not create door. reason: $reason")
//          }                    
        }
      }
    } ~
    path("create"){
      println(s"got create")
      post {
        entity(as[Door]) { door =>
          println(s"create door $door")
          onComplete(doorsManager ? ThingsManager.Create(door)) {
            case Success(status) => {
              status match {
                case AlreadyExists(id) => {
                  println(s"door $id already exists")
                  complete(400, s"door $id already exists")
                }
                case _ => {
                  println(s"door ${door.name} not found. creating")
                  complete(201, s"door created. state: $status")
                }
              }
            }
            case Failure(reason) => complete(s"could not create door. reason: $reason")
          }          
        }
      }
    }
  }
}