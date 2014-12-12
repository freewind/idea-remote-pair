package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair.{CreateDocumentConfirmation, Content, CreateDocument, MySpecification}

class DocumentSpec extends MySpecification {

  "CreateDocument" should {
    "response only version 0 if the its just be created" in new ProtocolMocking {
      client(context1).createOrJoinProject("test1")
      client(context1).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      there was one(context1).writeAndFlush(CreateDocumentConfirmation("/aaa", 0, None).toMessage)
    }
    "response only version if the content in server is the same as given" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test1")
      client(context1).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      client(context2).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      there was one(context2).writeAndFlush(CreateDocumentConfirmation("/aaa", 0, None).toMessage)
    }
    "response version and original content if content is not same as given" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test1")
      client(context1).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      client(context2).send(CreateDocument("/aaa", Content("abc123", "UTF-8")))
      there was one(context2).writeAndFlush(CreateDocumentConfirmation("/aaa", 0, Some(Content("abc", "UTF-8"))).toMessage)
    }
    "response version and original content if charset is not same" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test1")
      client(context1).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      client(context2).send(CreateDocument("/aaa", Content("abc", "GBK")))
      there was one(context2).writeAndFlush(CreateDocumentConfirmation("/aaa", 0, Some(Content("abc", "UTF-8"))).toMessage)
    }
    "allow to create document for different paths" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test1")
      client(context1).send(CreateDocument("/aaa", Content("abc", "UTF-8")))
      client(context2).send(CreateDocument("/bbb", Content("abc", "GBK")))
      there was one(context1).writeAndFlush(CreateDocumentConfirmation("/aaa", 0, None).toMessage)
      there was one(context2).writeAndFlush(CreateDocumentConfirmation("/bbb", 0, None).toMessage)
    }
  }

}
