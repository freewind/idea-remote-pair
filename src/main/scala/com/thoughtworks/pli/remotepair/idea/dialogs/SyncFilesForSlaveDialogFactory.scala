package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.{ClientName, PairEventListeners}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

object SyncFilesForSlaveDialogFactory {
  type SyncFilesForSlaveDialog = SyncFilesForSlaveDialogFactory#create
}

case class SyncFilesForSlaveDialogFactory(currentProject: RichProject, ClientName: ClientName, chooseIgnoreDialogFactory: WatchFilesDialogFactory, invokeLater: InvokeLater, pairEventListeners: PairEventListeners) {
  factory =>

  case class create() extends _SyncFilesBaseDialog with JDialogSupport {
    def invokeLater = factory.invokeLater
    def currentProject = factory.currentProject
    def pairEventListeners = factory.pairEventListeners

    @volatile var diffCount: Option[Int] = None
    @volatile var synced: Int = 0

    monitorReadEvent {
      case WatchingFiles(ClientName(name), _, fileSummaries) =>
        tabs.addTab(name, currentProject.getPairableFileSummaries, fileSummaries)
      case MasterWatchingFiles(_, _, _, diff) =>
        diffCount = Some(diff)
        okButton.setText(s"$synced / $diffCount")
      case event: SyncFileEvent =>
        synced += 1
        if (Some(synced) == diffCount) {
          okButton.setText("Complete!")
        } else {
          okButton.setText(s"$synced / $diffCount")
        }
    }

    onWindowOpened {
      for {
        conn <- currentProject.connection
        myId <- currentProject.myClientId
        masterId <- currentProject.masterClientId
      } conn.publish(GetWatchingFilesFromPair(myId, masterId))
    }

    onClick(configButton) {
      chooseIgnoreDialogFactory.create().showOnCenter()
    }

    onClick(cancelButton) {
      dispose()
    }

    onClick(okButton) {
      for {
        conn <- currentProject.connection
        clientId <- currentProject.clientInfo.map(_.clientId)
        watchingFiles <- currentProject.projectInfo.map(_.watchingFiles)
        fileSummaries = currentProject.getAllWatchingiles(watchingFiles).flatMap(currentProject.getFileSummary)
      } conn.publish(SyncFilesRequest(clientId, fileSummaries))
    }

  }

}
