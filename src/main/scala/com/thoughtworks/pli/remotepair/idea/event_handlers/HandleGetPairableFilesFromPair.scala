package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{GetWatchingFilesFromPair, WatchingFiles}
import com.thoughtworks.pli.remotepair.idea.core.PublishEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

case class HandleGetPairableFilesFromPair(currentProject: RichProject, publishEvent: PublishEvent) {
  def apply(event: GetWatchingFilesFromPair): Unit = {
    for {
      myClientId <- currentProject.clientInfo.map(_.clientId)
      fileSummaries = currentProject.getAllWatchingiles(currentProject.watchingFiles).flatMap(currentProject.getFileSummary)
    } publishEvent(new WatchingFiles(myClientId, event.fromClientId, fileSummaries))
  }

}
