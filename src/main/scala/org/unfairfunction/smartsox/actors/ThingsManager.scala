package org.unfairfunction.smartsox.actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import akka.actor.Terminated

object ThingsManager {

  trait Command
  
  trait Event

  val maxThings = 40
  val thingsToKillAtOnce = 20
  
}

trait ThingsManager extends Actor with ActorLogging {
  import Thing._
  import ThingsManager._
  
  
  def processCommand: Receive

  override def receive = processCommand orElse defaultProcessCommand

  private def defaultProcessCommand: Receive = {
    case Terminated(actor) => log.info(s"actor $actor terminated")
  }
  
  def processThingCommand(thingId: String, command: Thing.Command) = {
    val maybeChild = context child thingId
    maybeChild match {
      case Some(child) =>
        child forward command
      case None =>
        val child = create(thingId)
        child forward command
    }
  }

  protected def findOrCreate(id: String): ActorRef =
    context child id getOrElse create(id)

  protected def create(id: String): ActorRef = {
    val nextThing = context.actorOf(thingProps(id), id)
    context watch nextThing
    nextThing
  }
  
  def thingProps(id: String): Props
}