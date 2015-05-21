package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{Content, GetDocumentSnapshot, MoveCaretEvent}
import com.thoughtworks.pli.intellij.remotepair.utils.NewUuid
import com.thoughtworks.pli.remotepair.core.client.{GetMyClientId, InWatchingList, PublishCreateDocumentEvent, PublishEvent}
import com.thoughtworks.pli.remotepair.core.models.MyPlatform
import com.thoughtworks.pli.remotepair.core.{ClientVersionedDocuments, IsReadonlyMode, PluginLogger}

import scala.util.{Failure, Success}

class HandleDocumentChangeEvent(myPlatform: MyPlatform, publishEvent: PublishEvent, publishCreateDocumentEvent: PublishCreateDocumentEvent, newUuid: NewUuid, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments, inWatchingList: InWatchingList, isReadonlyMode: IsReadonlyMode, getMyClientId: GetMyClientId) {
  def apply(event: EditorDocumentChangeEvent): Unit = {
    if (inWatchingList(event.file) && !isReadonlyMode()) {
      myPlatform.invokeLater {
        event.file.relativePath.foreach { path =>
          clientVersionedDocuments.find(path) match {
            case Some(versionedDoc) => versionedDoc.synchronized {
              val content = event.document.content
              versionedDoc.submitContent(content) match {
                case Success(true) => publishEvent(MoveCaretEvent(path, event.editor.caret))
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
      event.file.relativePath.foreach { path =>
        clientVersionedDocuments.find(path) match {
          case Some(versionedDoc) => versionedDoc.latestContent match {
            case Some(Content(content, _)) if content != event.document.content => event.document.setContent(content)
            case _ =>
          }
          case _ =>
        }
      }
    }
  }
}
