package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.MyIde
import com.thoughtworks.pli.remotepair.idea.idea.GetProjectWindow
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

object SyncFilesForMasterDialog {
  type Factory = () => SyncFilesForMasterDialog
}

class SyncFilesForMasterDialog(val myPlatform: MyIde, myClient: MyClient, watchFilesDialogFactory: WatchFilesDialog.Factory, val pairEventListeners: PairEventListeners, val getProjectWindow: GetProjectWindow)
  extends _SyncFilesBaseDialog with JDialogSupport {

  onWindowOpened {
    if (myClient.isConnected) {
      for {
        myId <- myClient.myClientId
        otherId <- myClient.otherClients.map(_.clientId)
      } myClient.publishEvent(GetWatchingFilesFromPair(myId, otherId))
    }
  }

  monitorReadEvent {
    case WatchingFiles(fromClientId, _, fileSummaries) => myClient.clientIdToName(fromClientId).foreach { name =>
      tabs.addTab(name, fileSummaries, myClient.watchingFileSummaries)
    }
    case SyncFilesRequest(fromClientId, _) => myClient.clientIdToName(fromClientId).foreach { name =>
      tabs.setMessage(name, "Remote pair is requesting files")
    }
  }

  monitorWrittenEvent {
    case SyncFilesForAll =>
      okButton.setText("Synchronizing ...")
      okButton.setEnabled(false)
    case MasterWatchingFiles(_, toClientId, _, diffCount) => myClient.clientIdToName(toClientId).foreach(name => tabs.setTotalCount(name, diffCount))
    case SyncFileEvent(_, toClientId, _, _) => myClient.clientIdToName(toClientId).foreach(name => tabs.increase(name))
  }

  onClick(configButton) {
    watchFilesDialogFactory(None).showOnCenter()
  }

  onClick(cancelButton) {
    dispose()
  }

  onClick(okButton) {
    myClient.publishEvent(SyncFilesForAll)
  }

}
