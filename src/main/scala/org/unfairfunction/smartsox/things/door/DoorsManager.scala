package org.unfairfunction.smartsox.things.door

import org.unfairfunction.smartsox.actors.ThingsManager

object DoorsManager {
  import ThingsManager._
  
  
}

class DoorsManager extends ThingsManager {
  import DoorsManager._
  
  override def processCommand = {
    case _ =>
  }
  
  override def thingProps(id: String) = Door.props(id)
  
}