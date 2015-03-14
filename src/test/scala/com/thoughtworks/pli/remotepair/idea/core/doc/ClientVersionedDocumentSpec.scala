package com.thoughtworks.pli.remotepair.idea.core.doc

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.utils._
import com.thoughtworks.pli.remotepair.idea.MocksModule
import com.thoughtworks.pli.remotepair.idea.core._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class ClientVersionedDocumentSpec extends Specification with Mockito with MocksModule {

  isolated

  override lazy val clientVersionedDocumentFactory: ClientVersionedDocumentFactory = wire[ClientVersionedDocumentFactory]

  var uuid = 0
  newUuid.apply() returns {
    uuid += 1
    "uuid-" + uuid.toString
  }

  val doc = clientVersionedDocumentFactory.create("/aaa")
  val creation = new CreateDocumentConfirmation("/aaa", 0, Content("abc123", "UTF-8"))

  doc.handleCreation(creation)

  "submitContent" should {
    "return empty diff list if the submit content is equal to latest content" in {
      doc.submitContent("abc123")
      there was no(publishEvent).apply(any)
    }
    "publish one event if the submit content is not equal to latest content" in {
      doc.submitContent("c123xy")
      there was one(publishEvent).apply(ChangeContentEvent("uuid-1", "/aaa", 0, Seq(Delete(0, 2), Insert(4, "xy"))))
    }
    "publish only one event if the submit content multi-times" in {
      doc.submitContent("c123xy")
      doc.submitContent("c123xyzzzz")
      there was one(publishEvent).apply(ChangeContentEvent("uuid-1", "/aaa", 0, Seq(Delete(0, 2), Insert(4, "xy"))))
    }
  }

  "handleContentChange" should {
    "return insert diff list if the submit content is not equal to latest content" in {
      val change = ChangeContentConfirmation("uuid-some-event-id", "/aaa", 1, Seq(Insert(6, "def")))
      doc.handleContentChange(change, currentContent = "c123xyz") === Some("c123xyzdef")
    }
    "return insert diff list for multi-times changes handling" in {
      val change1 = ChangeContentConfirmation("uuid-some-event-id", "/aaa", 1, Seq(Insert(6, "def")))
      doc.handleContentChange(change1, currentContent = "c123xyz") === Some("c123xyzdef")

      // editor will apply the previous diffs, so the content will be `c123xyzdef`

      val change2 = ChangeContentConfirmation("uuid-some-event-id", "/aaa", 2, Seq(Insert(9, "456")))
      doc.handleContentChange(change2, currentContent = "c123xyzdef") === Some("c123xyzdef456")
    }
    "resolve pending and waiting diffs" in {
      doc.submitContent("c123xxx")
      doc.submitContent("c123xxxyyy")
      doc.handleContentChange(ChangeContentConfirmation("uuid-non-1", "/aaa", 1, Seq(Insert(6, "ddd"))), "c123xxxyyy") === None
      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 2, Seq(Delete(0, 2), Insert(4, "xxx"))), "c123xxxyyy") === Some("c123xxxyyyddd")

      doc.handleContentChange(ChangeContentConfirmation("uuid-2", "/aaa", 3, Seq(Insert(7, "kkk"))), "c123xxxyyyddd") === Some("c123xxxkkkyyyddd")
    }

    "resolve pending and waiting diffs2" in {
      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 2, Seq(Insert(2, "ddd"), Insert(6, "xxx"))), "abc123") === None
      doc.handleContentChange(ChangeContentConfirmation("uuid-non-1", "/aaa", 1, Seq(Insert(2, "ddd"))), "abc123") === Some("abddddxxxddc123")

    }

    "update version and content each time" in {
      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 1, Seq(Insert(6, "ddd"))), "any")
      (doc.latestVersion, doc.latestContent) ===(Some(1), Some(Content("abc123ddd", "UTF-8")))

      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 2, Seq(Insert(9, "eee"))), "any")
      (doc.latestVersion, doc.latestContent) ===(Some(2), Some(Content("abc123dddeee", "UTF-8")))
    }
    "do nothing is the version of coming change is not in order" in {
      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 3, Seq(Insert(6, "ddd"))), "any") === None
      (doc.latestVersion, doc.latestContent) ===(Some(0), Some(Content("abc123", "UTF-8")))
    }
    "apply the possible available changes when there are changes in order" in {
      doc.handleContentChange(ChangeContentConfirmation("uuid-3", "/aaa", 3, Seq(Insert(12, "ddd"))), "any") === None
      doc.handleContentChange(ChangeContentConfirmation("uuid-1", "/aaa", 1, Seq(Insert(6, "eee"))), "abc123") === Some("abc123eee")
      (doc.latestVersion, doc.latestContent) ===(Some(1), Some(Content("abc123eee", "UTF-8")))
      doc.handleContentChange(ChangeContentConfirmation("uuid-2", "/aaa", 2, Seq(Insert(9, "fff"))), "abc123eee") === Some("abc123eeefffddd")
      (doc.latestVersion, doc.latestContent) ===(Some(3), Some(Content("abc123eeefffddd", "UTF-8")))
    }
  }

  "handleCreation" should {
    "set the base version and content" in {
      doc.latestVersion === Some(0)
      doc.latestContent === Some(Content("abc123", "UTF-8"))
    }
    "ignore multiple events from the 2nd time" in {
      doc.handleCreation(new CreateDocumentConfirmation("/aaa", 1, Content("www", "UTF-8")))
      doc.latestVersion === Some(0)
      doc.latestContent === Some(Content("abc123", "UTF-8"))
    }
  }

}
