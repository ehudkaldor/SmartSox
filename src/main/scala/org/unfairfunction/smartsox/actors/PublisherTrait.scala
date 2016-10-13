package org.unfairfunction.smartsox.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.cluster.pubsub.DistributedPubSub


trait PublisherTrait extends Actor with ActorLogging {
  
   val mediator = DistributedPubSub(context.system).mediator
   val topicList: List[String]
   
   def publish {
     
   }
}

abstract class PublishMessage(flowId: Int, seqId: Int = 0)