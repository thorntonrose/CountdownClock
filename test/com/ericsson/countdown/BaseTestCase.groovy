package com.ericsson.countdown

import org.apache.log4j.*

/**
 * Base class for test cases.
 *
 * @author ethorro
 */
class BaseTestCase extends GroovyTestCase {
   private static final log = Logger.getLogger(BaseTestCase)

   protected void setUp() {
      super.setUp()
      log.info("")
      log.info("*** Set Up: ${this.class.simpleName}.$name ***")
   }

   protected void tearDown() {
      super.tearDown()
      log.info("")
      log.info("*** Tear Down: ${this.class.simpleName}.$name ***")
   }

   protected void runTest() {
      println name
      log.info("")
      log.info("*** Run Test: ${this.class.simpleName}.$name ***")
      super.runTest()
   }
}
