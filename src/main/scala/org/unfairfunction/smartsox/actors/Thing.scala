package org.unfairfunction.smartsox.actors

import org.json4s.JsonDSL._
import org.json4s.JsonAST.JValue
import org.json4s.DefaultFormats
import org.json4s.FieldSerializer
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTime
import org.json4s.ext.JodaTimeSerializers

//import org.

trait Thing {
  implicit val formats = DefaultFormats + FieldSerializer[Thing]() ++ JodaTimeSerializers.all
    
  val name: String
  val created: DateTime = DateTime.now
}