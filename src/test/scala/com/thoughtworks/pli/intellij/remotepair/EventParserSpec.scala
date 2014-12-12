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
      parse( """ServerStatusResponse {"projects":[{"name":"myname","clients":[{"clientId":"123","project":"test1","name":"user222","isMaster":true}],"ignoredFiles":[],"workingMode":"CaretSharing"}],"freeClients":0}""",
        ServerStatusResponse(Seq(ProjectInfoData("myname", Seq(ClientInfoResponse("123", "test1", "user222", isMaster = true)), Nil, WorkingMode.CaretSharing)), freeClients = 0))
    }
    "parse SyncFileEvent" in {
      parse( """SyncFileEvent {"path":"/aaa","content":{"text":"my-content","charset":"UTF-8"}}""", SyncFileEvent("/aaa", Content("my-content", "UTF-8")))
    }
    "parse SyncFilesRequest" in {
      parse( """SyncFilesRequest {"fromClientId":"id1","fileSummaries":[{"path":"/aaa","summary":"s1"}]}""", SyncFilesRequest("id1", Seq(FileSummary("/aaa", "s1"))))
    }
    "parse CreateDocument" in {
      parse( """CreateDocument {"path":"/aaa","content":{"text":"my-content","charset":"UTF-8"}}""", CreateDocument("/aaa", Content("my-content", "UTF-8")))
    }
    "parse CreateDocumentConfirmation with content" in {
      parse( """CreateDocumentConfirmation {"path":"/aaa","version":12,"content":{"text":"my-content","charset":"UTF-8"}}""", CreateDocumentConfirmation("/aaa", 12, Some(Content("my-content", "UTF-8"))))
    }
    "parse CreateDocumentConfirmation without content" in {
      parse( """CreateDocumentConfirmation {"path":"/aaa","version":12}""", CreateDocumentConfirmation("/aaa", 12, None))
    }
  }
}
