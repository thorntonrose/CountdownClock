package com.ericsson.countdown

import java.util.concurrent.*
import org.apache.log4j.*

/**
 * CountdownResource tests.
 *
 * @author ethorro
 */
class ClockResourceTest extends BaseTestCase {
   private static final log = Logger.getLogger(ClockResourceTest)
   def clockResource

   protected void setUp() {
      super.setUp()

      clockResource = new ClockResource()
      clockResource.stop()
   }

   //--------------------------------------------------------------------------

   void testSetTime() {
      def resp = clockResource.setTime "24:00:00"
      log.debug "resp: $resp"
      assertEquals("status:", 204, resp.status)

      def time = Json.fromJson(clockResource.getTime().entity).time
      log.debug "time: $time"
      assertEquals("time:", "24:00:00", time)
   }

   void testGetTime() {
      clockResource.setTime "23:00"

      def resp = clockResource.getTime()
      log.debug "resp: $resp"
      assertEquals("status:", 200, resp.status)

      def time = Json.fromJson(resp.entity).time
      log.debug "time: $time"
      assertEquals("time:", "00:23:00", time)
   }

   void testStart() {
      clockResource.setTime "24:00:00"

      def resp = clockResource.start()
      assertEquals("status:", 204, resp.status)
      assertNotNull("thread == null", Clock.thread)
   }

   void testStop() {
      clockResource.setTime "24:00:00"
      clockResource.start()

      println "sleep ..."
      TimeUnit.SECONDS.sleep(1)

      def resp = clockResource.stop()
      assertEquals("status:", 204, resp.status)
      assertNull("thread != null", Clock.thread)
   }

   void testCountdown() {
      clockResource.setTime "5"
      def resp = clockResource.start()

      println "sleep ..."
      TimeUnit.SECONDS.sleep(5)
      assertNull("thread != null", Clock.thread)

      def time = Json.fromJson(clockResource.getTime().entity).time
      log.debug "time: $time"
      assertEquals("time:", "00:00:00", time)
   }

   void testSetTime_Error() {
      def resp = clockResource.setTime("0w90r59")
      assertEquals("status:", 400, resp.status)
   }
}
