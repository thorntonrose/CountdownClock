package com.ericsson.countdown

import java.util.concurrent.*
import javax.ws.rs.*
import javax.ws.rs.core.*
import org.apache.log4j.*

/**
 * Resource for countdown operations.
 *
 * @author ethorro
 */
@Path("clock")
class ClockResource {
   private static final log = Logger.getLogger(ClockResource.class)

   @PUT
   @Path("{time}")
   Response setTime(@PathParam("time") String time) {
      log.debug "set: time: ${time} ..."

      try {
         Clock.time = time
         Response.status(204).build()
      } catch(Exception e) {
         Response.status(400).entity(e.message).build()
      }
   }

   @GET
   Response getTime() {
      log.debug "get ..."
      Response.status(200).entity(Json.toJson([ time: Clock.time ])).build()
   }

   @POST
   @Path("start")
   Response start() {
      log.debug "start ..."
      Clock.start()
      Response.status(204).build()
   }

   @POST
   @Path("stop")
   Response stop() {
      log.debug "stop ..."
      Clock.stop()
      Response.status(204).build()
   }
}
