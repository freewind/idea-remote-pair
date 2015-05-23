package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocument, Content, GetDocumentSnapshot, MoveCaretEvent}
import com.thoughtworks.pli.intellij.remotepair.utils.NewUuid
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.{MyFile, MyIde}
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.server_event_handlers.ClientVersionedDocuments

import scala.util.{Failure, Success}

class HandleDocumentChangeEvent(myPlatform: MyIde, myClient: MyClient, newUuid: NewUuid, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments) {
  def apply(event: EditorDocumentChangeEvent): Unit = {
    if (myClient.isWatching(event.file) && !myClient.isReadonlyMode) {
      myPlatform.invokeLater {
        event.file.relativePath.foreach { path =>
          clientVersionedDocuments.find(path) match {
            case Some(versionedDoc) => versionedDoc.synchronized {
              val content = event.document.content
              versionedDoc.submitContent(content) match {
                case Success(true) => myClient.publishEvent(MoveCaretEvent(path, event.editor.caret))
                case Failure(e) => myClient.myClientId.foreach(myId => myClient.publishEvent(GetDocumentSnapshot(myId, path)))
                case _ =>
              }
            }
            case None => publishCreateDocumentEvent(event.file)
          }
        }
      }
    }

    if (myClient.isReadonlyMode) {
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

  private def publishCreateDocumentEvent(file: MyFile): Unit = file.relativePath.foreach { path =>
    myClient.publishEvent(CreateDocument(path, file.content))
  }

}
