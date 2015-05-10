package com.thoughtworks.pli.remotepair.core.server_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocumentConfirmation, Content, DocumentSnapshotEvent}
import com.thoughtworks.pli.remotepair.idea.MocksModule
import com.thoughtworks.pli.remotepair.core.server_event_handlers.document.HandleDocumentSnapshotEvent
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleDocumentSnapshotEventSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleDocumentSnapshotEvent = new HandleDocumentSnapshotEvent(clientVersionedDocuments, pluginLogger, getMyClientName, writeToProjectFile, runWriteAction, openFileInTab)

  val event = new DocumentSnapshotEvent("/abc", 3, Content("Hello", "UTF-8"))

  "When client receives DocumentSnapshotEvent, it" should {
    "update ClientVersionedDocuments map with new information" in {
      handleDocumentSnapshotEvent(event)
      there was one(clientVersionedDocuments).create(CreateDocumentConfirmation("/abc", 3, Content("Hello", "UTF-8")))
    }
    "update corresponding file content" in {
      handleDocumentSnapshotEvent(event)
      there was one(writeToProjectFile).apply("/abc", Content("Hello", "UTF-8"))
    }
    "open it in new tab" in {
      handleDocumentSnapshotEvent(event)
      there was one(openFileInTab).apply(event.path)
    }
  }

}
