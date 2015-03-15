package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class SyncFilesForMasterDialogFactory(connectionHolder: ConnectionHolder, chooseIgnoreDialogFactory: WatchFilesDialogFactory, ClientName: ClientName, invokeLater: InvokeLater, pairEventListeners: PairEventListeners, getProjectWindow: GetProjectWindow, getMyClientId: GetMyClientId, getOtherClients: GetOtherClients, getWatchingFileSummaries: GetWatchingFileSummaries) {
  factory =>

  case class create() extends _SyncFilesBaseDialog with JDialogSupport {
    override def invokeLater = factory.invokeLater
    override def getProjectWindow = factory.getProjectWindow
    override def pairEventListeners = factory.pairEventListeners

    onWindowOpened {
      connectionHolder.get.foreach { conn =>
        for {
          myId <- getMyClientId()
          otherId <- getOtherClients().map(_.clientId)
        } conn.publish(GetWatchingFilesFromPair(myId, otherId))
      }
    }

    monitorReadEvent {
      case WatchingFiles(ClientName(name), _, fileSummaries) =>
        tabs.addTab(name, fileSummaries, getWatchingFileSummaries())
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
      connectionHolder.get.foreach { conn =>
        conn.publish(SyncFilesForAll)
      }
    }
  }

}
