package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{ChangeContentConfirmation, Content}
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.core.editors.HighlightNewContent
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

class HandleChangeContentConfirmation(publishEvents: PublishEvent, runWriteAction: RunWriteAction, logger: Logger, clientVersionedDocuments: ClientVersionedDocuments, getFileByRelative: GetFileByRelative, writeToProjectFile: WriteToProjectFile, getCachedFileContent: GetCachedFileContent, getFileContent: GetFileContent, highlightContent: HighlightNewContent, synchronized: Synchronized) {

  def apply(event: ChangeContentConfirmation): Unit = getFileByRelative(event.path).foreach { file =>
    val doc = clientVersionedDocuments.get(event.path)
    runWriteAction {
      try {
        synchronized(doc) {
          val Content(currentContent, charset) = tryBestToGetFileContent(file)
          doc.handleContentChange(event, currentContent).map { targetContent =>
            writeToProjectFile(event.path, Content(targetContent, charset))
            highlightContent(event.path, targetContent)
          }
        }
      } catch {
        case e: Throwable => logger.error("Error occurs when handling ChangeContentConfirmation: " + e.toString, e)
      }
    }
  }

  private def tryBestToGetFileContent(file: VirtualFile) = {
    getCachedFileContent(file).getOrElse(getFileContent(file))
  }

}
