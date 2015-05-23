package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{Content, GetDocumentSnapshot, MoveCaretEvent}
import com.thoughtworks.pli.intellij.remotepair.utils.NewUuid
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.MyPlatform
import com.thoughtworks.pli.remotepair.core.{ClientVersionedDocuments, PluginLogger}

import scala.util.{Failure, Success}

class HandleDocumentChangeEvent(myPlatform: MyPlatform, connectedClient: ConnectedClient, publishCreateDocumentEvent: PublishCreateDocumentEvent, newUuid: NewUuid, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments) {
  def apply(event: EditorDocumentChangeEvent): Unit = {
    if (connectedClient.isWatching(event.file) && !connectedClient.isReadonlyMode) {
      myPlatform.invokeLater {
        event.file.relativePath.foreach { path =>
          clientVersionedDocuments.find(path) match {
            case Some(versionedDoc) => versionedDoc.synchronized {
              val content = event.document.content
              versionedDoc.submitContent(content) match {
                case Success(true) => connectedClient.publishEvent(MoveCaretEvent(path, event.editor.caret))
                case Failure(e) => connectedClient.myClientId.foreach(myId => connectedClient.publishEvent(GetDocumentSnapshot(myId, path)))
                case _ =>
              }
            }
            case None => publishCreateDocumentEvent(event.file)
          }
        }
      }
    }

    if (connectedClient.isReadonlyMode) {
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
