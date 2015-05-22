package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.MyPlatform
import com.thoughtworks.pli.remotepair.idea.idea.GetProjectWindow
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

object SyncFilesForMasterDialog {
  type Factory = () => SyncFilesForMasterDialog
}

class SyncFilesForMasterDialog(val myPlatform: MyPlatform, connectedClient: ConnectedClient, watchFilesDialogFactory: WatchFilesDialog.Factory, val pairEventListeners: PairEventListeners, val getProjectWindow: GetProjectWindow)
  extends _SyncFilesBaseDialog with JDialogSupport {

  onWindowOpened {
    connectedClient.connectionHolder.get.foreach { conn =>
      for {
        myId <- connectedClient.getMyClientId
        otherId <- connectedClient.getOtherClients.map(_.clientId)
      } conn.publish(GetWatchingFilesFromPair(myId, otherId))
    }
  }

  monitorReadEvent {
    case WatchingFiles(fromClientId, _, fileSummaries) => connectedClient.clientIdToName(fromClientId).foreach { name =>
      tabs.addTab(name, fileSummaries, connectedClient.getWatchingFileSummaries)
    }
    case SyncFilesRequest(fromClientId, _) => connectedClient.clientIdToName(fromClientId).foreach { name =>
      tabs.setMessage(name, "Remote pair is requesting files")
    }
  }

  monitorWrittenEvent {
    case SyncFilesForAll =>
      okButton.setText("Synchronizing ...")
      okButton.setEnabled(false)
    case MasterWatchingFiles(_, toClientId, _, diffCount) => connectedClient.clientIdToName(toClientId).foreach(name => tabs.setTotalCount(name, diffCount))
    case SyncFileEvent(_, toClientId, _, _) => connectedClient.clientIdToName(toClientId).foreach(name => tabs.increase(name))
  }

  onClick(configButton) {
    watchFilesDialogFactory(None).showOnCenter()
  }

  onClick(cancelButton) {
    dispose()
  }

  onClick(okButton) {
    connectedClient.connectionHolder.get.foreach { conn =>
      conn.publish(SyncFilesForAll)
    }
  }

}
