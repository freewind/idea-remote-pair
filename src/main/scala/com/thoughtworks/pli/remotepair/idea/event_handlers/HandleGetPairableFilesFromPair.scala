package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{GetPairableFilesFromPair, PairableFiles}
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.PublishEvent

case class HandleGetPairableFilesFromPair(currentProject: RichProject, publishEvent: PublishEvent) {
  def apply(event: GetPairableFilesFromPair): Unit = {
    for {
      myClientId <- currentProject.clientInfo.map(_.clientId)
      fileSummaries = currentProject.getAllPairableFiles(currentProject.ignoredFiles).flatMap(currentProject.getFileSummary)
    } publishEvent(new PairableFiles(myClientId, event.fromClientId, fileSummaries))
  }

}
