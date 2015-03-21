package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.idea.MocksModule
import com.thoughtworks.pli.remotepair.idea.core.ClientVersionedDocument
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class ProjectDocumentListenerFactorySpec extends Specification with Mockito with MocksModule {

  isolated

  override lazy val projectDocumentListenerFactory: ProjectDocumentListenerFactory = wire[ProjectDocumentListenerFactory]

  val (editor, file, event, versionedDoc) = (mock[Editor], mock[VirtualFile], mock[DocumentEvent], mock[ClientVersionedDocument])

  val listener = projectDocumentListenerFactory.createNewListener(editor, file, currentProject)

  inWatchingList.apply(file) returns true
  getRelativePath.apply(file) returns Some("/abc")
  clientVersionedDocuments.find("/abc") returns Some(versionedDoc)
  getDocumentContent(event) returns "HelloWorld"
  versionedDoc.submitContent("HelloWorld") returns true
  getCaretOffset.apply(editor) returns 3

  "When document changes, ProjectDocumentListener" should {
    "publish create document event if the document has no version information" in {
      clientVersionedDocuments.find("/abc") returns None
      listener.documentChanged(event)
      there was one(publishCreateDocumentEvent).apply(file)
    }
    "publish submit new content to versioned document if the document has version information already" in {
      listener.documentChanged(event)
      there was one(versionedDoc).submitContent("HelloWorld")
    }
    "publish move caret event if successfully submit new content" in {
      listener.documentChanged(event)
      there was one(publishEvent).apply(MoveCaretEvent("/abc", 3))
    }

    "not publish move caret event if failed to submit new content" in {
      versionedDoc.submitContent("HelloWorld") returns false
      listener.documentChanged(event)
      there was no(publishEvent).apply(any[MoveCaretEvent])
    }
    "not do anything if the file is not in watching list" in {
      inWatchingList.apply(file) returns false
      listener.documentChanged(event)
      there was no(clientVersionedDocuments).find(any)
    }
  }
}
