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
    "parse ServerStatusResponse" in {
      parse( """ServerStatusResponse {"projects":[{"name":"sdfsdf","clients":[{"project":"sdfsdf","ip":"192.168.1.127","name":"user222","isMaster":true}],"ignoredFiles":[],"workingMode":"CaretSharing"}],"freeClients":[]}""",
        ServerStatusResponse(Seq(ProjectInfoData("sdfsdf", Seq(ClientInfoResponse(Some("sdfsdf"), "192.168.1.127", "user222", isMaster = true)), Nil, WorkingMode.CaretSharing)), Nil))
    }
  }
}
