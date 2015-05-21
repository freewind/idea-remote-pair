package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{DocumentAdapter, DocumentEvent, DocumentListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.editor_event_handlers.{HandleIdeaEvent, EditorDocumentChangeEvent}
import com.thoughtworks.pli.remotepair.idea.models._

class ProjectDocumentListenerFactory(logger: PluginLogger, handleIdeaEvent: HandleIdeaEvent, ideaFactories: IdeaFactories)
  extends ListenerManager[DocumentListener] {
  val key = new Key[DocumentListener]("remote_pair.listeners.document")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): DocumentListener = new DocumentAdapter {

    override def documentChanged(event: DocumentEvent): Unit = {
      logger.info("documentChanged event: " + event)
      handleIdeaEvent(new EditorDocumentChangeEvent(ideaFactories(file), ideaFactories(editor), ideaFactories(event.getDocument)))
    }
  }

  override def originAddListener(editor: Editor) = editor.getDocument.addDocumentListener

  override def originRemoveListener(editor: Editor) = editor.getDocument.removeDocumentListener
}

