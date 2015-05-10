package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{ChangeContentConfirmation, Content, GetDocumentSnapshot}
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.{PublishEvent, GetMyClientId, GetMyClientName}
import com.thoughtworks.pli.remotepair.idea.editor.HighlightNewContent
import com.thoughtworks.pli.remotepair.idea.file.{GetCachedFileContent, GetFileContent, WriteToProjectFile}
import com.thoughtworks.pli.remotepair.idea.project.GetFileByRelative
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

import scala.util.{Failure, Success}

class HandleChangeContentConfirmation(publishEvent: PublishEvent, runWriteAction: RunWriteAction, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments, getFileByRelative: GetFileByRelative, writeToProjectFile: WriteToProjectFile, getCachedFileContent: GetCachedFileContent, getFileContent: GetFileContent, highlightContent: HighlightNewContent, synchronized: Synchronized, getMyClientId: GetMyClientId, getMyClientName: GetMyClientName) {

  def apply(event: ChangeContentConfirmation): Unit = {
    (getFileByRelative(event.path), clientVersionedDocuments.find(event.path)) match {
      case (Some(file), Some(doc)) => runWriteAction {
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
    getMyClientId().foreach(myId => publishEvent(GetDocumentSnapshot(myId, event.path)))
  }

  private def tryBestToGetFileContent(file: VirtualFile) = {
    getCachedFileContent(file).getOrElse(getFileContent(file))
  }

}
