package com.thoughtworks.pli.remotepair.core.ui.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.VirtualButton

import scala.language.reflectiveCalls

trait VirtualSyncFilesForMasterDialog extends BaseVirtualDialog {
  def myClient: MyClient
  def dialogFactories: DialogFactories

  val okButton: VirtualButton
  val cancelButton: VirtualButton
  val configButton: VirtualButton
  val tabs: {
    def addTab(title: String, fileSummaries: Seq[FileSummary], watchingFileSummaries: Seq[FileSummary])
    def setMessage(title: String, message: String)
    def setTotalCount(name: String, diffCount: Int): Unit
    def increase(name: String): Unit
  }

  dialog.onOpen {
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
      okButton.text_=("Synchronizing ...")
      okButton.enabled = false
    case MasterWatchingFiles(_, toClientId, _, diffCount) => myClient.clientIdToName(toClientId).foreach(name => tabs.setTotalCount(name, diffCount))
    case SyncFileEvent(_, toClientId, _, _) => myClient.clientIdToName(toClientId).foreach(name => tabs.increase(name))
  }

  configButton.onClick {
    dialogFactories.createWatchFilesDialog(None).showOnCenter()
  }

  cancelButton.onClick {
    dialog.dispose()
  }

  okButton.onClick {
    myClient.publishEvent(SyncFilesForAll)
  }

}
