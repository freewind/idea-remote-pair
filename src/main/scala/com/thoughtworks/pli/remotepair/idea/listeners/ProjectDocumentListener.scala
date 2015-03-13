package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.event.{DocumentEvent, DocumentListener}
import com.intellij.openapi.editor.{Document, Editor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.intellij.remotepair.utils.{ContentDiff, Delete, Insert, NewUuid}
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class ProjectDocumentListener(currentProject: RichProject, invokeLater: InvokeLater, publishEvent: PublishEvent, publishCreateDocumentEvent: PublishCreateDocumentEvent, newUuid: NewUuid, logger: Logger, versionedDocuments: ClientVersionedDocuments) extends ListenerManageSupport[DocumentListener] {
  val key = new Key[DocumentListener]("remote_pair.listeners.document")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): DocumentListener = {
    new DocumentListener {

      override def documentChanged(event: DocumentEvent): Unit = invokeLater {
        logger.info("## documentChanged: " + event)
        currentProject.getRelativePath(file).foreach { path =>
          versionedDocuments.find(path) match {
            case Some(versionedDoc) => versionedDoc.synchronized {
              val content = event.getDocument.getCharsSequence.toString
              if (versionedDoc.submitContent(content)) {
                publishEvent(MoveCaretEvent(path, editor.getCaretModel.getOffset))
              }
            }
            case None => publishCreateDocumentEvent(file)
          }
        }
      }

      private def applyDiff(doc: Document)(diff: ContentDiff) = diff match {
        case Insert(offset, content) => doc.insertString(offset, content)
        case Delete(offset, length) => doc.deleteString(offset, offset + length)
      }

      override def beforeDocumentChange(event: DocumentEvent) {
        logger.info("## beforeDocumentChanged: " + event)
      }
    }
  }

  override def originAddListener(editor: Editor) = editor.getDocument.addDocumentListener

  override def originRemoveListener(editor: Editor) = editor.getDocument.removeDocumentListener
}

