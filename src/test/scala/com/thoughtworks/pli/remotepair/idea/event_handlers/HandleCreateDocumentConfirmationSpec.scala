package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{Content, CreateDocumentConfirmation}
import com.thoughtworks.pli.remotepair.idea.MocksModule
import com.thoughtworks.pli.remotepair.idea.core.ClientVersionedDocument
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleCreateDocumentConfirmationSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleCreateDocumentConfirmation = new HandleCreateDocumentConfirmation(writeToProjectFile, runWriteAction, clientVersionedDocuments)

  val event = new CreateDocumentConfirmation("/abc", version = 22, content = Content("hello", "UTF-8"))

  clientVersionedDocuments.find("/abc") returns None

  "When client receives CreateDocumentConfirmation, it" should {
    "create a versioned document if no existing versioned document found" in {
      handleCreateDocumentConfirmation(event)
      there was one(clientVersionedDocuments).create(event)
    }
    "write content to new file if no existing versioned document found" in {
      handleCreateDocumentConfirmation(event)
      there was one(writeToProjectFile).apply("/abc", Content("hello", "UTF-8"))
    }
    "do nothing if existed corresponding versioned document" in {
      clientVersionedDocuments.find("/abc") returns Some(mock[ClientVersionedDocument])
      there was no(clientVersionedDocuments).create(any)
    }
  }
}
