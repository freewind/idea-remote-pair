package com.thoughtworks.pli.intellij.remotepair

class EventParserSpec extends MySpecification {

  val parser = new EventParser {}

  def parse(line: String, expectedEvent: PairEvent) = {
    val event = parser.parseEvent(line)
    event === expectedEvent
  }

  "EventParser" should {
    "parse AskForJoinProject" in {
      parse("AskForJoinProject {}", AskForJoinProject(None))
      parse( """AskForJoinProject {"message":"project not found"}""", AskForJoinProject(Some("project not found")))
    }
    "parse ServerErrorResponse" in {
      parse( """ServerErrorResponse {"message":"test-error"}""", ServerErrorResponse("test-error"))
    }
    "parse ServerStatusResponse" in {
      parse( """ServerStatusResponse {"projects":[{"clientId":"123","name":"sdfsdf","clients":[{"project":"sdfsdf","ip":"192.168.1.127","name":"user222","isMaster":true}],"ignoredFiles":[],"workingMode":"CaretSharing"}],"freeClients":[]}""",
        ServerStatusResponse(Seq(ProjectInfoData("sdfsdf", Seq(ClientInfoResponse("123", "sdfsdf", "user222", isMaster = true)), Nil, WorkingMode.CaretSharing)), freeClients = 0))
    }
    "parse SyncFileEvent" in {
      parse( """SyncFileEvent {"path":"/aaa","content":{"text":"my-content","charset":"UTF-8"}}""", SyncFileEvent("/aaa", Content("my-content", "UTF-8")))
    }
    "parse SyncFilesRequest" in {
      parse( """SyncFilesRequest {"from":"aaa","files":[{"path":"/aaa","summary":"s1"}]}""", SyncFilesRequest("aaa", Seq(FileSummary("/aaa", "s1"))))
    }
  }
}
