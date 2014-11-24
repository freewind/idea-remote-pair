package com.thoughtworks.pli.intellij.remotepair

import com.thoughtworks.pli.intellij.MySpecification

class EventParserSpec extends MySpecification {

  val parser = new EventParser {}

  def parse(line: String, expectedEvent: PairEvent) = {
    val event = parser.parseEvent(line)
    event === expectedEvent
  }

  "EventParser" should {
    "parse AskForClientInformation" in {
      parse("AskForClientInformation {}", AskForClientInformation)
    }
    "parse AskForJoinProject" in {
      parse("AskForJoinProject {}", AskForJoinProject)
    }
    "parse ServerErrorResponse" in {
      parse( """ServerErrorResponse {"message":"test-error"}""", ServerErrorResponse("test-error"))
    }
  }
}
