package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.ResetTabRequest
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.PublishEvent
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater
import com.intellij.openapi.diagnostic.Logger

case class TabEventHandler(currentProject: RichProject,
                      invokeLater: InvokeLater,
                      publishEvent: PublishEvent,
                      publishSyncFilesRequest: PublishSyncFilesRequest, log: Logger) {

  def handleOpenTabEvent(path: String) = {
    openTab(path)(currentProject)
  }

  def handleCloseTabEvent(path: String) = {
    currentProject.getFileByRelative(path).foreach(file => invokeLater(currentProject.fileEditorManager.closeFile(file)))
  }

  private def openTab(path: String)(project: RichProject) {
    currentProject.getFileByRelative(path) match {
      case Some(file) =>
        val openFileDescriptor = project.openFileDescriptor(file)
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
