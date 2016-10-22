package org.unfairfunction.smartsox.actors

import java.util.Date

trait Thing {
  val id: Long    
}

trait ThingMessage {
  val id: Long
  val timestamp: Date
}
  
trait Trigger extends ThingMessage {
    
}
  
trait Update extends ThingMessage {
    
}
