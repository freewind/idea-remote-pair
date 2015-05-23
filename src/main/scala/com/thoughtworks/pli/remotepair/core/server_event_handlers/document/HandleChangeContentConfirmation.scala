package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import com.thoughtworks.pli.intellij.remotepair.protocol.{ChangeContentConfirmation, Content, GetDocumentSnapshot}
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyFile, MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.ClientVersionedDocuments
import com.thoughtworks.pli.remotepair.idea.editor.HighlightNewContent

import scala.util.{Failure, Success}

class HandleChangeContentConfirmation(currentProject: MyProject, myClient: MyClient, myPlatform: MyIde, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments, highlightContent: HighlightNewContent) {

  def apply(event: ChangeContentConfirmation): Unit = {
    (currentProject.getFileByRelative(event.path), clientVersionedDocuments.find(event.path)) match {
      case (Some(file), Some(doc)) => myPlatform.runWriteAction {
        try {
          doc.synchronized {
            val Content(currentContent, _) = tryBestToGetFileContent(file)
            doc.handleContentChange(event, currentContent) match {
              case Success(Some(targetContent)) =>
                currentProject.getTextEditorsOfPath(event.path) match {
                  case Nil => currentProject.findOrCreateFile(event.path).setContent(targetContent)
                  case editors => editors.foreach(_.document.modifyTo(targetContent))
                }
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
    myClient.myClientId.foreach(myId => myClient.publishEvent(GetDocumentSnapshot(myId, event.path)))
  }

  private def tryBestToGetFileContent(file: MyFile) = {
    file.cachedContent.getOrElse(file.content)
  }

}
