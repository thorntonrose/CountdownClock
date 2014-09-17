package com.ericsson.countdown

import groovy.json.*

/**
 * JSON utility methods.
 *
 * @author ethorro
 */
class Json {
   private Json() {
   }

   static toJson(obj) {
      new JsonBuilder(obj).toPrettyString()
   }

   static fromJson(text) {
      new JsonSlurper().parseText(text)
   }
}
