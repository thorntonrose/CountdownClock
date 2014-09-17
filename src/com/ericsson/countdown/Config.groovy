package com.ericsson.countdown

import groovy.util.*
import java.io.*
import java.util.*
import org.apache.log4j.*

/**
 * Application configuration.
 *
 * @author ethorro
 */
class Config {
   private static final log = Logger.getLogger(Config)
   static env = System.properties.env ?: "run"
   static props = new ConfigSlurper(env).parse(Config.class.classLoader.getResource("app.conf"))

   static {
      log.info "<clinit>: env: $env"
   }

   private Config() {
   }
}
