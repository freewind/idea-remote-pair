package com.thoughtworks.pli.intellij.remotepair.listeners

import com.intellij.openapi.editor.event.{DocumentEvent, DocumentListener}
import com.intellij.openapi.util.Key
import com.intellij.openapi.editor.Editor
import com.thoughtworks.pli.intellij.remotepair.{AppLogger, PublishEvents, ChangeContentEvent}
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.utils.Md5Support
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder

trait DocumentListenerSupport extends PublishEvents with AppLogger {
  this: CurrentProjectHolder =>

  def createDocumentListener() = new ListenerManageSupport[DocumentListener] {
    val key = new Key[DocumentListener]("remote_pair.listeners.document")

    def createNewListener(editor: Editor, file: VirtualFile, project: Project): DocumentListener = {
      new DocumentListener with Md5Support {

        override def documentChanged(event: DocumentEvent) {
          log.info("## documentChanged: " + event)
          val summary = md5(event.getDocument.getCharsSequence.toString)
          val eee = ChangeContentEvent(currentProject.getRelativePath(file), event.getOffset, event.getOldFragment.toString, event.getNewFragment.toString, summary)
          publishEvent(eee)
        }

        override def beforeDocumentChange(event: DocumentEvent) {
          log.info("## beforeDocumentChanged: " + event)
        }
      }
    }

    override def originAddListener(editor: Editor) = editor.getDocument.addDocumentListener

    override def originRemoveListener(editor: Editor) = editor.getDocument.removeDocumentListener
  }
}
