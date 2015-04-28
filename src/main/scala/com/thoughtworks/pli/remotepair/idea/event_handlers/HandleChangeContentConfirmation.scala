package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{GetDocumentSnapshot, ChangeContentConfirmation, Content}
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.core.editors.HighlightNewContent
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

import scala.util.{Failure, Success}

class HandleChangeContentConfirmation(publishEvent: PublishEvent, runWriteAction: RunWriteAction, logger: Logger, clientVersionedDocuments: ClientVersionedDocuments, getFileByRelative: GetFileByRelative, writeToProjectFile: WriteToProjectFile, getCachedFileContent: GetCachedFileContent, getFileContent: GetFileContent, highlightContent: HighlightNewContent, synchronized: Synchronized, getMyClientId: GetMyClientId, getMyClientName: GetMyClientName) {

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
              case Success(_) =>
              case Failure(e) => ???
            }
          }
        } catch {
          case e: Throwable => logger.error("Error occurs when handling ChangeContentConfirmation: " + e.toString, e)
        }
      }
      case _ => getMyClientId().foreach(myId => publishEvent(GetDocumentSnapshot(myId, event.path)))
    }
  }

  private def tryBestToGetFileContent(file: VirtualFile) = {
    getCachedFileContent(file).getOrElse(getFileContent(file))
  }

}
