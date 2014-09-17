package com.ericsson.countdown

import java.net.*
import javax.ws.rs.*
import javax.ws.rs.core.*

/**
 * Resource that redirects to index page.
 *
 * @author ethorro
 */
@Path("")
class IndexResource {
   @GET
   Response get() {
      Response.temporaryRedirect(new URI("index.html")).build()
   }
}
