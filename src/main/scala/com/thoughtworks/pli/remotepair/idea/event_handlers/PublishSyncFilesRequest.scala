package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.SyncFilesRequest
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.{GetMyClientId, GetServerWatchingFiles, PublishEvent}

case class PublishSyncFilesRequest(currentProject: RichProject, publishEvent: PublishEvent, getServerWatchingFiles: GetServerWatchingFiles, getMyClientId: GetMyClientId) {

  def apply(watchingFiles: Seq[String] = getServerWatchingFiles()): Unit = getMyClientId() match {
    case Some(clientId) => val files = currentProject.getAllWatchingiles(watchingFiles).flatMap(currentProject.getFileSummary)
      publishEvent(SyncFilesRequest(clientId, files))
    case None =>
  }

}
