package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.{ClientName, PairEventListeners}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class SyncFilesForMasterDialogFactory(currentProject: RichProject, chooseIgnoreDialogFactory: WatchFilesDialogFactory, ClientName: ClientName, invokeLater: InvokeLater, pairEventListeners: PairEventListeners) {
  factory =>

  case class create() extends _SyncFilesBaseDialog with JDialogSupport {
    override def invokeLater = factory.invokeLater
    override def currentProject = factory.currentProject
    override def pairEventListeners = factory.pairEventListeners

    onWindowOpened {
      currentProject.connection.foreach { conn =>
        for {
          myId <- currentProject.myClientId
          otherId <- currentProject.otherClientIds
        } conn.publish(GetWatchingFilesFromPair(myId, otherId))
      }
    }

    monitorReadEvent {
      case WatchingFiles(ClientName(name), _, fileSummaries) =>
        tabs.addTab(name, fileSummaries, currentProject.getPairableFileSummaries)
      case SyncFilesRequest(ClientName(name), _) =>
        tabs.setMessage(name, "Remote pair is requesting files")
    }

    monitorWrittenEvent {
      case SyncFilesForAll =>
        okButton.setText("Synchronizing ...")
        okButton.setEnabled(false)
      case MasterWatchingFiles(_, ClientName(name), _, diffCount) => tabs.setTotalCount(name, diffCount)
      case SyncFileEvent(_, ClientName(name), _, _) => tabs.increase(name)
    }

    onClick(configButton) {
      chooseIgnoreDialogFactory.create().showOnCenter()
    }

    onClick(cancelButton) {
      dispose()
    }

    onClick(okButton) {
      currentProject.connection.foreach { conn =>
        conn.publish(SyncFilesForAll)
      }
    }
  }

}
