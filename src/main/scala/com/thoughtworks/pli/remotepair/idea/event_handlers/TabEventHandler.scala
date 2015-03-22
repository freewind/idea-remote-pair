package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.intellij.remotepair.protocol.ResetTabRequest
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater


case class TabEventHandler(getFileByRelative: GetFileByRelative,
                           invokeLater: InvokeLater,
                           publishEvent: PublishEvent,
                           closeFile: CloseFile,
                           getOpenFileDescriptor: GetOpenFileDescriptor,
                           publishSyncFilesRequest: PublishSyncFilesRequest, logger: Logger) {

  def handleOpenTabEvent(path: String) = {
    openTab(path)
  }

  def handleCloseTabEvent(path: String) = {
    getFileByRelative(path).foreach(file => invokeLater(closeFile(file)))
  }

  private def openTab(path: String) {
    getFileByRelative(path) match {
      case Some(file) =>
        val openFileDescriptor = getOpenFileDescriptor(file)
        if (openFileDescriptor.canNavigate) {
          invokeLater(openFileDescriptor.navigate(true))
        }
      case _ => invokeLater {
        publishSyncFilesRequest.apply()
        publishEvent.apply(ResetTabRequest)
      }
    }
  }

}
