package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

object SyncFilesForMasterDialog {
  type Factory = () => SyncFilesForMasterDialog
}

class SyncFilesForMasterDialog(connectionHolder: ConnectionHolder, watchFilesDialogFactory: WatchFilesDialog.Factory, clientIdToName: ClientIdToName, val invokeLater: InvokeLater, val pairEventListeners: PairEventListeners, val getProjectWindow: GetProjectWindow, getMyClientId: GetMyClientId, getOtherClients: GetOtherClients, getWatchingFileSummaries: GetWatchingFileSummaries)
  extends _SyncFilesBaseDialog with JDialogSupport {

  onWindowOpened {
    connectionHolder.get.foreach { conn =>
      for {
        myId <- getMyClientId()
        otherId <- getOtherClients().map(_.clientId)
      } conn.publish(GetWatchingFilesFromPair(myId, otherId))
    }
  }

  monitorReadEvent {
    case WatchingFiles(fromClientId, _, fileSummaries) => clientIdToName(fromClientId).foreach { name =>
      tabs.addTab(name, fileSummaries, getWatchingFileSummaries())
    }
    case SyncFilesRequest(fromClientId, _) => clientIdToName(fromClientId).foreach { name =>
      tabs.setMessage(name, "Remote pair is requesting files")
    }
  }

  monitorWrittenEvent {
    case SyncFilesForAll =>
      okButton.setText("Synchronizing ...")
      okButton.setEnabled(false)
    case MasterWatchingFiles(_, toClientId, _, diffCount) => clientIdToName(toClientId).foreach(name => tabs.setTotalCount(name, diffCount))
    case SyncFileEvent(_, toClientId, _, _) => clientIdToName(toClientId).foreach(name => tabs.increase(name))
  }

  onClick(configButton) {
    watchFilesDialogFactory(None).showOnCenter()
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
