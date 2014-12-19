package com.thoughtworks.pli.intellij.remotepair.listeners

import com.intellij.openapi.editor.{Document, Editor}
import com.intellij.openapi.editor.event.{DocumentEvent, DocumentListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.utils.UuidSupport
import com.thoughtworks.pli.intellij.remotepair._

trait DocumentListenerSupport extends PublishEvents with AppLogger with InvokeLater {
  this: CurrentProjectHolder =>

  def createDocumentListener() = new ListenerManageSupport[DocumentListener] {
    val key = new Key[DocumentListener]("remote_pair.listeners.document")

    def createNewListener(editor: Editor, file: VirtualFile, project: Project): DocumentListener = {
      new DocumentListener with UuidSupport {

        override def documentChanged(event: DocumentEvent) :Unit = invokeLater{
          log.info("## documentChanged: " + event)
          val path = currentProject.getRelativePath(file)
          currentProject.versionedDocuments.find(path).foreach { versionedDoc =>
            versionedDoc.sync {
              val content = event.getDocument.getCharsSequence.toString
              versionedDoc.submitContent(content)
            }
          }
        }

        private def applyDiff(doc: Document)(diff: ContentDiff) = diff match {
          case Insert(offset, content) => doc.insertString(offset, content)
          case Delete(offset, length) => doc.deleteString(offset, offset + length)
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
