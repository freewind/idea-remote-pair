package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair._

class DocumentSpec extends MySpecification {

  "CreateDocument" should {
    "response version 0 and init content if the its just be created" in new ProtocolMocking {
      client(context1).createOrJoinProject("test1")
      client(context1).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      there was one(context1).writeAndFlush(CreateDocumentConfirmation("/aaa", 0, Content("abc", "UTF-8")).toMessage)
    }
    "response version and original content if content is not same as given" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test1")
      client(context1).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      client(context2).send(CreateDocument("/aaa", Content("abc123", "UTF-8")))
      there was one(context2).writeAndFlush(CreateDocumentConfirmation("/aaa", 0, Content("abc", "UTF-8")).toMessage)
    }
    "response version and original content if charset is not same" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test1")
      client(context1).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      client(context2).send(CreateDocument("/aaa", Content("abc", "GBK")))
      there was one(context2).writeAndFlush(CreateDocumentConfirmation("/aaa", 0, Content("abc", "UTF-8")).toMessage)
    }
    "allow to create document for different paths" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test1")
      client(context1).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      client(context2).send(CreateDocument("/bbb", Content("abc", "GBK")))
      there was one(context1).writeAndFlush(CreateDocumentConfirmation("/aaa", 0, Content("abc", "UTF-8")).toMessage)
      there was one(context2).writeAndFlush(CreateDocumentConfirmation("/bbb", 0, Content("abc", "GBK")).toMessage)
    }
  }

  "ChangeContentEvent" should {
    "get confirmation with new version (== oldVersion+1) and changes based on old version if no conflict" in new ProtocolMocking {
      client(context1).createOrJoinProject("test1")
      client(context1).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      client(context1).send(ChangeContentEvent("eventId1", "/aaa", 0, Seq(Insert(3, "123"))))
      there was one(context1).writeAndFlush(ChangeContentConfirmation("eventId1", "/aaa", 1, Seq(Insert(3, "123")), "abc123"))
    }
    "get confirmation with new version (== serverLatestVersion+1) and changes based on old version if there is conflict" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test1")
      client(context1).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      client(context2).send(ChangeContentEvent("eventId1", "/aaa", 0, Seq(Insert(3, "111"))))
      client(context1).send(ChangeContentEvent("eventId2", "/aaa", 0, Seq(Insert(3, "222"))))
      there was one(context1).writeAndFlush(ChangeContentConfirmation("eventId2", "/aaa", 2, Seq(Insert(3, "111222")), "abc111222"))
    }
  }
}
