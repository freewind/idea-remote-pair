package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{DocumentAdapter, DocumentEvent, DocumentListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{GetDocumentSnapshot, Content, MoveCaretEvent}
import com.thoughtworks.pli.intellij.remotepair.utils._
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

import scala.util.{Failure, Success}

class ProjectDocumentListenerFactory(invokeLater: InvokeLater, publishEvent: PublishEvent, publishCreateDocumentEvent: PublishCreateDocumentEvent, newUuid: NewUuid, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments, inWatchingList: InWatchingList, getRelativePath: GetRelativePath, getDocumentContent: GetDocumentContent, getCaretOffset: GetCaretOffset, isReadonlyMode: IsReadonlyMode, getMyClientId: GetMyClientId)
  extends ListenerManager[DocumentListener] {
  val key = new Key[DocumentListener]("remote_pair.listeners.document")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): DocumentListener = new DocumentAdapter {

    override def documentChanged(event: DocumentEvent): Unit = {
      logger.info("documentChanged event: " + event)
      if (inWatchingList(file) && !isReadonlyMode()) {
        invokeLater {
          getRelativePath(file).foreach { path =>
            clientVersionedDocuments.find(path) match {
              case Some(versionedDoc) => versionedDoc.synchronized {
                val content = getDocumentContent(event)
                versionedDoc.submitContent(content) match {
                  case Success(true) => publishEvent(MoveCaretEvent(path, getCaretOffset(editor)))
                  case Failure(e) => getMyClientId().foreach(myId => publishEvent(GetDocumentSnapshot(myId, path)))
                  case _ =>
                }
              }
              case None => publishCreateDocumentEvent(file)
            }
          }
        }
      }

      if (isReadonlyMode()) {
        getRelativePath(file).foreach { path =>
          clientVersionedDocuments.find(path) match {
            case Some(versionedDoc) => versionedDoc.latestContent match {
              case Some(Content(content, _)) if content != getDocumentContent(event.getDocument) => event.getDocument.setText(content)
              case _ =>
            }
            case _ =>
          }
        }
      }
    }
  }

  override def originAddListener(editor: Editor) = editor.getDocument.addDocumentListener

  override def originRemoveListener(editor: Editor) = editor.getDocument.removeDocumentListener
}

