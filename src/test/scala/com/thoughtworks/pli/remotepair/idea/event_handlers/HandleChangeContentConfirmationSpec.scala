package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{ChangeContentConfirmation, Content, GetDocumentSnapshot}
import com.thoughtworks.pli.remotepair.idea.MocksModule
import com.thoughtworks.pli.remotepair.idea.core.{ClientVersionedDocument, PendingChangeTimeoutException}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.util.{Failure, Success}

class HandleChangeContentConfirmationSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleChangeContentConfirmation = new HandleChangeContentConfirmation(publishEvent, runWriteAction, logger, clientVersionedDocuments, getFileByRelative, writeToProjectFile, getCachedFileContent, getFileContent, highlightNewContent, synchronized, getMyClientId, getMyClientName)

  val event = ChangeContentConfirmation(forEventId = "test-event-id", path = "/abc", newVersion = 10, diffs = Nil)

  val virtualFile = mock[VirtualFile]
  val doc = mock[ClientVersionedDocument]
  val editor = mock[Editor]

  getFileByRelative.apply("/abc") returns Some(virtualFile)
  getCachedFileContent.apply(virtualFile) returns Some(Content("cached-file-content", "UTF-8"))
  getFileContent.apply(virtualFile) returns Content("my-file-content", "UTF-8")
  clientVersionedDocuments.find("/abc") returns Some(doc)
  getMyClientId.apply() returns Some("my-client-id")

  "When received HandleChangeContentConfirmation, it" should {
    "update the corresponding file with new calculated content" in {
      doc.handleContentChange(event, "cached-file-content") returns Success(Some("new-content1"))
      handleChangeContentConfirmation(event)
      there was one(writeToProjectFile).apply("/abc", Content("new-content1", "UTF-8"))
    }
    "highlight new content" in {
      doc.handleContentChange(event, "cached-file-content") returns Success(Some("new-content1"))
      handleChangeContentConfirmation(event)
      there was one(highlightNewContent).apply("/abc", "new-content1")
    }
    "use file content if cache file content is not found" in {
      doc.handleContentChange(event, "my-file-content") returns Success(Some("new-content2"))
      getCachedFileContent.apply(virtualFile) returns None
      handleChangeContentConfirmation(event)
      there was one(writeToProjectFile).apply("/abc", Content("new-content2", "UTF-8"))
    }
    "do nothing if file is not found by path" in {
      getFileByRelative.apply("/abc") returns None
      handleChangeContentConfirmation(event)
      there was no(getCachedFileContent).apply(any)
    }
    "not write to file if the content is not changed" in {
      doc.handleContentChange(any, any) returns Success(None)
      handleChangeContentConfirmation(event)
      there was no(writeToProjectFile).apply(any, any)
    }
    "publish GetDocumentSnapshot if no ClientVersionedDocument found" in {
      clientVersionedDocuments.find("/abc") returns None
      handleChangeContentConfirmation(event)
      there was one(publishEvent).apply(GetDocumentSnapshot("my-client-id", event.path))
    }
    "publish GetDocumentSnapshot if got PendingChangeTimeoutException when operating on ClientVersionedDocument" in {
      doc.handleContentChange(any, any) returns Failure(mock[PendingChangeTimeoutException])
      handleChangeContentConfirmation(event)
      there was one(publishEvent).apply(GetDocumentSnapshot("my-client-id", event.path))
    }
  }

}
