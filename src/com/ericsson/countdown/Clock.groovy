package com.ericsson.countdown

import java.util.concurrent.*
import java.text.*

class Clock {
   static def duration
   static def thread

   private Clock() {
   }

   static def getTime() {
      def hours = TimeUnit.SECONDS.toHours(duration)
      def minutes = TimeUnit.SECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(hours)
      def seconds = duration - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes)

      new Formatter().format("%02d:%02d:%02d", hours, minutes, seconds).toString()
   }

   // format: [[HH:]mm:]ss
   static def setTime(time) {
      def (seconds, minutes, hours) = time.split(":").reverse().collect { it as int }
      duration = (hours ? TimeUnit.HOURS.toSeconds(hours) : 0) +
         (minutes ? TimeUnit.MINUTES.toSeconds(minutes) : 0) + seconds
   }

   static def start() {
      if ((! thread) && (duration > 0)) {
         thread = Thread.start {
            while (true) {
               duration --
               if (duration == 0) { break }

               try {
                  TimeUnit.SECONDS.sleep(1)
               } catch(InterruptedException) {
                  break
               }
            }

            thread = null
         }
      }
   }

   static def stop() {
      if (thread) { thread.interrupt() }
      thread = null
   }
}