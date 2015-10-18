package com.thoughtworks.pli.remotepair.core.doc

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.utils._
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.server_event_handlers.ClientVersionedDocument
import com.thoughtworks.pli.remotepair.idea.MyMockito
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

trait MocksModule {
  this: Mockito =>
  val logger = mock[PluginLogger]
  val myClient = mock[MyClient]
  val myUtils = new MyUtils {
    override val newUuid = mock[NewUuid]
  }
  val mySystem = mock[MySystem]
}

class ClientVersionedDocumentSpec extends Specification with MyMockito with MocksModule {

  isolated

  lazy val clientVersionedDocumentFactory: ClientVersionedDocument.Factory = new ClientVersionedDocument(_)(logger, myClient, myUtils, mySystem)

  var uuid = 0
  myUtils.newUuid.apply() returns {
    uuid += 1
    "uuid-" + uuid.toString
  }

  val creation = new CreateDocumentConfirmation("/aaa", 0, Content("abc123", "UTF-8"))
  val doc = clientVersionedDocumentFactory(creation)
  mySystem.now returns 1

  val callback = mock[Boolean => Unit]

  "submitContent" should {
    "如果提交的内容与已有内容相同,则不会向server发布信息,也不会调用callback" in {
      doc.submitContent(() => "abc123", callback)
      there was no(myClient).publishEvent(any)
      there was no(callback).apply(any)
    }
    "如果提交的内容与已有的内容不同,则会向server发布增量修改信息,并且将true传入callback" in {
      doc.submitContent(() => "c123xy", callback)
      there was one(myClient).publishEvent(ChangeContentEvent("uuid-1", "/aaa", 0, Seq(Delete(0, 2), Insert(4, "xy"))))
      there was one(callback).apply(true)
    }
    "如果连续提交多次内容,只有第一次的可以成功发送出去,后面几次因为第一次的没有收到server的确认而被忽略" in {
      doc.submitContent(() => "c123xy", callback)
      there was one(callback).apply(true)
      there was one(myClient).publishEvent(ChangeContentEvent("uuid-1", "/aaa", 0, Seq(Delete(0, 2), Insert(4, "xy"))))

      reset(callback, myClient)

      doc.submitContent(() => "c123xyzzzz", callback)
      there was no(myClient).publishEvent(any)
      there was no(callback).apply(any)
    }
    "如果上次提交的信息在2秒内没有收到server的确认信息,则会把false传入callback" in {
      doc.submitContent(() => "any", _ => ())
      mySystem.now returns 2002 // later than 2s
      doc.submitContent(() => "any-other", callback)
      there was one(callback).apply(false)
    }
  }

  //  "handleContentChange" should {
  //    "return insert diff list if the submit content is not equal to latest content" in {
  //      val change = ChangeContentConfirmation("uuid-some-event-id", "/aaa", 1, Seq(Insert(6, "def")))
  //      doc.handleContentChange(change, currentContent = "c123xyz") === Success(Some("c123xyzdef"))
  //    }
  //    "return insert diff list for multi-times changes handling" in {
  //      val change1 = ChangeContentConfirmation("uuid-some-event-id", "/aaa", 1, Seq(Insert(6, "def")))
  //      doc.handleContentChange(change1, currentContent = "c123xyz") === Success(Some("c123xyzdef"))
  //
  //      // editor will apply the previous diffs, so the content will be `c123xyzdef`
  //
  //      val change2 = ChangeContentConfirmation("uuid-some-event-id", "/aaa", 2, Seq(Insert(9, "456")))
  //      doc.handleContentChange(change2, currentContent = "c123xyzdef") === Success(Some("c123xyzdef456"))
  //    }
  //    "resolve pending and waiting diffs" in {
  //      doc.submitContent("c123xxx")
  //      doc.submitContent("c123xxxyyy")
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-non-1", "/aaa", 1, Seq(Insert(6, "ddd"))), "c123xxxyyy") === Success(None)
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 2, Seq(Delete(0, 2), Insert(4, "xxx"))), "c123xxxyyy") === Success(Some("c123xxxyyyddd"))
  //
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-2", "/aaa", 3, Seq(Insert(7, "kkk"))), "c123xxxyyyddd") === Success(Some("c123xxxkkkyyyddd"))
  //    }
  //
  //    "resolve pending and waiting diffs2" in {
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 2, Seq(Insert(2, "ddd"), Insert(6, "xxx"))), "abc123") === Success(None)
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-non-1", "/aaa", 1, Seq(Insert(2, "ddd"))), "abc123") === Success(Some("abddddxxxddc123"))
  //
  //    }
  //
  //    "update version and content each time" in {
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 1, Seq(Insert(6, "ddd"))), "any")
  //      (doc.latestVersion, doc.latestContent) ===(Some(1), Some(Content("abc123ddd", "UTF-8")))
  //
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 2, Seq(Insert(9, "eee"))), "any")
  //      (doc.latestVersion, doc.latestContent) ===(Some(2), Some(Content("abc123dddeee", "UTF-8")))
  //    }
  //    "do nothing is the version of coming change is not in order" in {
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 3, Seq(Insert(6, "ddd"))), "any") === Success(None)
  //      (doc.latestVersion, doc.latestContent) ===(Some(0), Some(Content("abc123", "UTF-8")))
  //    }
  //    "apply the possible available changes when there are changes in order" in {
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-3", "/aaa", 3, Seq(Insert(12, "ddd"))), "any") === Success(None)
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 1, Seq(Insert(6, "eee"))), "abc123") === Success(Some("abc123eee"))
  //      (doc.latestVersion, doc.latestContent) ===(Some(1), Some(Content("abc123eee", "UTF-8")))
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-2", "/aaa", 2, Seq(Insert(9, "fff"))), "abc123eee") === Success(Some("abc123eeefffddd"))
  //      (doc.latestVersion, doc.latestContent) ===(Some(3), Some(Content("abc123eeefffddd", "UTF-8")))
  //    }
  //    "return Failure if the pending change is timeout" in {
  //      doc.submitContent("any")
  //      getCurrentTimeMillis.apply() returns 2002 // later than 2s
  //      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 1, Seq(Insert(6, "ddd"))), "any") must beAFailedTry.withThrowable[PendingChangeTimeoutException]
  //    }
  //  }

  "handleCreation" should {
    "set the base version and content" in {
      doc.baseVersion === 0
      doc.baseContent === Content("abc123", "UTF-8")
    }
  }

}
