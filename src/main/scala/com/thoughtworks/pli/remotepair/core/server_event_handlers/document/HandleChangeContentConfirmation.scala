package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import com.thoughtworks.pli.intellij.remotepair.protocol.{ChangeContentConfirmation, Content, GetDocumentSnapshot}
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient
import com.thoughtworks.pli.remotepair.core.models.{MyFile, MyPlatform, MyProject}
import com.thoughtworks.pli.remotepair.idea.editor.HighlightNewContent
import com.thoughtworks.pli.remotepair.idea.file.WriteToProjectFile

import scala.util.{Failure, Success}

class HandleChangeContentConfirmation(currentProject: MyProject, connectedClient: ConnectedClient, myPlatform: MyPlatform, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments, writeToProjectFile: WriteToProjectFile, highlightContent: HighlightNewContent, synchronized: Synchronized) {

  def apply(event: ChangeContentConfirmation): Unit = {
    (currentProject.getFileByRelative(event.path), clientVersionedDocuments.find(event.path)) match {
      case (Some(file), Some(doc)) => myPlatform.runWriteAction {
        try {
          synchronized(doc) {
            val Content(currentContent, charset) = tryBestToGetFileContent(file)
            doc.handleContentChange(event, currentContent) match {
              case Success(Some(targetContent)) =>
                writeToProjectFile(event.path, Content(targetContent, charset))
                highlightContent(event.path, targetContent)
              case Failure(e) => requestSnapshot(event)
              case Success(None) =>
            }
          }
        } catch {
          case e: Throwable => logger.error("Error occurs when handling ChangeContentConfirmation: " + e.toString, e)
        }
      }
      case _ => requestSnapshot(event)
    }
  }

  def requestSnapshot(event: ChangeContentConfirmation): Unit = {
    connectedClient.myClientId.foreach(myId => connectedClient.publishEvent(GetDocumentSnapshot(myId, event.path)))
  }

  private def tryBestToGetFileContent(file: MyFile) = {
    file.cachedContent.getOrElse(file.content)
  }

}
