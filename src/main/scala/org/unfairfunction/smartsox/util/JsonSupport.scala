package org.unfairfunction.smartsox.util

import org.json4s.DefaultFormats
import org.json4s.native
import org.json4s.ext.JodaTimeSerializers
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

trait JsonSupport extends Json4sSupport {
    implicit val serialization = native.Serialization
    implicit val formats = DefaultFormats ++ JodaTimeSerializers.all
}