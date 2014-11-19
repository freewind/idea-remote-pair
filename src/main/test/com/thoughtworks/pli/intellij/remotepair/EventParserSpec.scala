package com.thoughtworks.pli.intellij.remotepair

import org.specs2.mutable.Specification

class EventParserSpec extends Specification {

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
    "parse AskForWorkingMode" in {
      parse("AskForWorkingMode {}", AskForWorkingMode)
    }
    "parse ServerErrorResponse" in {
      parse( """ServerErrorResponse {"message":"test-error"}""", ServerErrorResponse("test-error"))
    }
  }
}
