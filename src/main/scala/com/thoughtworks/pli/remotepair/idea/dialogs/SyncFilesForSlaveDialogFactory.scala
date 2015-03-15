package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

object SyncFilesForSlaveDialogFactory {
  type SyncFilesForSlaveDialog = SyncFilesForSlaveDialogFactory#create
}

case class SyncFilesForSlaveDialogFactory(ClientName: ClientName, chooseIgnoreDialogFactory: WatchFilesDialogFactory, invokeLater: InvokeLater, pairEventListeners: PairEventListeners, getProjectWindow: GetProjectWindow, getWatchingFileSummaries: GetWatchingFileSummaries, connectionHolder: ConnectionHolder, getMyClientId: GetMyClientId, getMasterClientId: GetMasterClientId, getAllClients: GetAllClients) {
  factory =>

  case class create() extends _SyncFilesBaseDialog with JDialogSupport {
    override def invokeLater = factory.invokeLater
    override def getProjectWindow = factory.getProjectWindow
    override def pairEventListeners = factory.pairEventListeners

    @volatile var diffCount: Option[Int] = None
    @volatile var synced: Int = 0

    monitorReadEvent {
      case WatchingFiles(ClientName(name), _, fileSummaries) =>
        tabs.addTab(name, getWatchingFileSummaries(), fileSummaries)
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
        conn <- connectionHolder.get
        myId <- getMyClientId()
        masterId <- getMasterClientId()
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
        conn <- connectionHolder.get
        clientId <- getAllClients().map(_.clientId)
        fileSummaries = getWatchingFileSummaries()
      } conn.publish(SyncFilesRequest(clientId, fileSummaries))
    }

  }

}
