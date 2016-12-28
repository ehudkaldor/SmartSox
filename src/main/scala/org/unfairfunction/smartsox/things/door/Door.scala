package org.unfairfunction.smartsox.things.door

import org.unfairfunction.smartsox.actors.Thing
import org.json4s.DefaultFormats
import org.json4s.FieldSerializer
import org.json4s.ext.JodaTimeSerializers
import org.joda.time.DateTime

case class Door(override val name: String) extends Thing {
//  implicit override val formats = DefaultFormats + FieldSerializer[Door]() ++ JodaTimeSerializers.all
}