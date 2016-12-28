package org.unfairfunction.smartsox.api

import akka.actor.{ActorSystem, Actor}
//import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
//import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.http.scaladsl.model._
//import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
//import scala.concurrent.Future
//import akka.NotUsed
import scala.io.StdIn
import org.unfairfunction.smartsox.util.HostIP
import org.unfairfunction.smartsox.things.door.DoorsManager
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import akka.http.scaladsl.server.directives.Credentials
import scala.concurrent.Future

//import org.unfairfunction.smartsox.util.NodeConfig

object Main {
  def main(args: Array[String]): Unit = {
    println("starting main")
    implicit val system = ActorSystem("system")
//    println("created system")
//    akka.Main.main(Array(classOf[ApiServer].getName))
    val apiserver = new ApiServer()
  }
}

//trait MyJsonSupport extends json with DefaultJsonProtocol {
////  implicit val heroFormat = jsonFormat2(Hero)
////  implicit def arrayWrapper[T: JsonFormat] = jsonFormat1(ArrayWrapper.apply[T])
//}

class ApiServer(implicit val system: ActorSystem)  {
  
    println("starting API server")
    
//    val nodeConfig = NodeConfig parse args

//    val index = "index.html"

    val port = 9001
    
    val listenAddress = "0.0.0.0"
  
    println("creating actor system")
//    implicit val system: ActorSystem = ActorSystem(nodeConfig.get.clusterName, nodeConfig.get.config)
//    implicit val system: ActorSystem = ActorSystem("system")
    implicit val materializer = ActorMaterializer.create(system)
    implicit val executionContext = system.dispatcher
    val sslConfig = AkkaSSLConfig()

    println("actor system created")
    
    val doorsManagerActorRef = system.actorOf(DoorsManager.props("doorsmanager"), "doorsmanager")
    
  def myUserPassAuthenticator(credentials: Credentials): Option[String] =
    credentials match {
      case p @ Credentials.Provided(id) if p.verify("ehud") => Some(id)
      case _ => None
    }

    val route =
      pathSingleSlash {
        get {
          complete("get single slash")
        }
      } ~
      pathPrefix("api") {
        authenticateBasic("api login", myUserPassAuthenticator){ username =>
          pathEnd {
//            println("pathEndOrSingleSlash")
            get { 
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>smartsox api server</h1>"))
            } 
          } ~
          pathPrefix("doors") {
//            pathEndOrSingleSlash {
//              get {
//                complete("doors")
//              }      
//            } ~
            DoorsManager.routes(doorsManagerActorRef)
          }   
        }
      } 
      
      
    val bindingFuture = Http().bindAndHandle(route, listenAddress, port)
    
    println(s"Server online at http://$listenAddress:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
    
 
//  def route =
//    pathSingleSlash {
//      get {
//        complete("")
//      }   
//    } ~
//    path("register") {
//      parameter('name) { name ⇒
//        handleWebSocketMessages(handler("ws://localhost:9001/register?name=" + name, name))
//      }
//    } 
//    ~ path("create" / Segment) { id =>
//      accounts.values.toList.contains(id) match {
//        case false => {
//          handleWebSocketMessages(Flow[Message].collect {
//            case TextMessage.Strict(txt) => txt
//          }.via(Flow.fromSinkAndSource(Sink.ignore, Source.single("You are not registered!!!!!!!!!!!")))
//            .map {
//              case msg: String => TextMessage.Strict(msg)
//            })
//        }
//        case true => handleWebSocketMessages(http.webSocketClientFlow(WebSocketRequest("ws://localhost:9002/create")))
//      }
//    }
    
//  def handler(url: String, name: String): Flow[Message, Message, Any] = {
//    http.webSocketClientFlow(WebSocketRequest(url)).map {
//      case TextMessage.Strict(txt) => {
//        if (txt contains ("You are already registered")) {
//          TextMessage(txt)
//        } else {
//          accounts += (name -> txt)
//          TextMessage(s"You are registered !!!! Use this ${txt} to create order!!")
//        }
//      }
//    }
//  }
}