package com.ericsson.countdown

import com.ericsson.maintv.util.*
import com.ericsson.maintv.crawler.*
import org.apache.log4j.*
import org.mortbay.jetty.*
import org.mortbay.jetty.webapp.*

/**
 * Application main class.
 *
 * @author ethorro
 */
class Main {
   private static final log = Logger.getLogger(Main)

   static void main(String[] args) {
      try {
         log.info "Server..."
         def server = new Server(Config.props.webapp.port)
         server.addHandler(new WebAppContext(contextPath: Config.props.webapp.context, war: "war"))
         server.start()
         server.join()
      } catch(Exception e) {
         log.error(e, "$e")
         println e
         System.exit(1)
      }
   }
}
