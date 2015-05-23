package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.MyPlatform
import com.thoughtworks.pli.remotepair.idea.idea.GetProjectWindow
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

object SyncFilesForSlaveDialog {
  type Factory = () => SyncFilesForSlaveDialog
}

class SyncFilesForSlaveDialog(myClient: MyClient, watchFilesDialogFactory: WatchFilesDialog.Factory, val myPlatform: MyPlatform, val pairEventListeners: PairEventListeners, val getProjectWindow: GetProjectWindow)
  extends _SyncFilesBaseDialog with JDialogSupport {

  @volatile var diffCount: Option[Int] = None
  @volatile var synced: Int = 0

  monitorReadEvent {
    case WatchingFiles(fromClientId, _, fileSummaries) => myClient.clientIdToName(fromClientId).foreach { name =>
      tabs.addTab(name, myClient.watchingFileSummaries, fileSummaries)
    }
    case MasterWatchingFiles(_, _, _, diff) =>
      if (diff == 0) {
        markAsComplete()
      } else {
        diffCount = Some(diff)
        okButton.setText(s"$synced / $diffCount")
      }
    case event: SyncFileEvent =>
      synced += 1
      if (Some(synced) == diffCount) {
        markAsComplete()
      } else {
        okButton.setText(s"$synced / $diffCount")
      }
  }

  onWindowOpened {
    if (myClient.isConnected) {
      for {
        myId <- myClient.myClientId
        masterId <- myClient.masterClientId
      } myClient.publishEvent(GetWatchingFilesFromPair(myId, masterId))
    }
  }

  onClick(configButton) {
    watchFilesDialogFactory(None).showOnCenter()
  }

  onClick(cancelButton) {
    dispose()
  }

  onClick(okButton) {
    if (myClient.isConnected) {
      for {
        clientId <- myClient.allClients.map(_.clientId)
        fileSummaries = myClient.watchingFileSummaries
      } myClient.publishEvent(SyncFilesRequest(clientId, fileSummaries))
    }
  }

  private def markAsComplete(): Unit = {
    okButton.setText("Complete!")
    onClick(okButton, clearAll = true) {
      dispose()
    }
  }

}

