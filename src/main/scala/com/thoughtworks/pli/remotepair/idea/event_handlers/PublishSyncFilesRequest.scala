package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.SyncFilesRequest
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.PublishEvent

case class PublishSyncFilesRequest(currentProject: RichProject, publishEvent: PublishEvent) {

  def apply(ignoredFiles: Seq[String] = currentProject.ignoredFiles): Unit = {
    val files = currentProject.getAllPairableFiles(ignoredFiles).flatMap(currentProject.getFileSummary)
    publishEvent(SyncFilesRequest(currentProject.clientInfo.get.clientId, files))
  }

}
