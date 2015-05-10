package com.thoughtworks.pli.remotepair.core.idea_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{Content, GetDocumentSnapshot, MoveCaretEvent}
import com.thoughtworks.pli.intellij.remotepair.utils.NewUuid
import com.thoughtworks.pli.remotepair.core.client.{GetMyClientId, InWatchingList, PublishCreateDocumentEvent, PublishEvent}
import com.thoughtworks.pli.remotepair.core.{ClientVersionedDocuments, IsReadonlyMode, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.editor.GetCaretOffset
import com.thoughtworks.pli.remotepair.idea.file.{GetDocumentContent, GetRelativePath}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

import scala.util.{Failure, Success}

class HandleDocumentChangeEvent(invokeLater: InvokeLater, publishEvent: PublishEvent, publishCreateDocumentEvent: PublishCreateDocumentEvent, newUuid: NewUuid, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments, inWatchingList: InWatchingList, getRelativePath: GetRelativePath, getDocumentContent: GetDocumentContent, getCaretOffset: GetCaretOffset, isReadonlyMode: IsReadonlyMode, getMyClientId: GetMyClientId) {
  def apply(event: IdeaDocumentChangeEvent): Unit = {
    if (inWatchingList(event.file) && !isReadonlyMode()) {
      invokeLater {
        getRelativePath(event.file).foreach { path =>
          clientVersionedDocuments.find(path) match {
            case Some(versionedDoc) => versionedDoc.synchronized {
              val content = getDocumentContent(event.document)
              versionedDoc.submitContent(content) match {
                case Success(true) => publishEvent(MoveCaretEvent(path, getCaretOffset(event.editor)))
                case Failure(e) => getMyClientId().foreach(myId => publishEvent(GetDocumentSnapshot(myId, path)))
                case _ =>
              }
            }
            case None => publishCreateDocumentEvent(event.file)
          }
        }
      }
    }

    if (isReadonlyMode()) {
      getRelativePath(event.file).foreach { path =>
        clientVersionedDocuments.find(path) match {
          case Some(versionedDoc) => versionedDoc.latestContent match {
            case Some(Content(content, _)) if content != getDocumentContent(event.document) => event.document.setText(content)
            case _ =>
          }
          case _ =>
        }
      }
    }
  }
}
