package com.thoughtworks.pli.remotepair.core.ui.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.VirtualButton
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

trait VirtualSyncFilesForSlaveDialog extends BaseVirtualDialog {
  def currentProject: MyProject
  def myClient: MyClient
  def dialogFactories: DialogFactories
  def myIde: MyIde
  def pairEventListeners: PairEventListeners

  @volatile var diffCount: Option[Int] = None
  @volatile var synced: Int = 0

  val okButton: VirtualButton
  val configButton: VirtualButton
  val cancelButton: VirtualButton
  val tabs: VirtualPairDifferentFileTabs

  override def init(): Unit = {
    monitorReadEvent {
      case WatchingFiles(fromClientId, _, fileSummaries) => myClient.clientIdToName(fromClientId).foreach { name =>
        tabs.addTab(name, myClient.watchingFileSummaries, fileSummaries)
      }
      case MasterWatchingFiles(_, _, _, diff) =>
        if (diff == 0) {
          markAsComplete()
        } else {
          diffCount = Some(diff)
          okButton.text_=(s"$synced / $diffCount")
        }
      case event: SyncFileEvent =>
        synced += 1
        if (Some(synced) == diffCount) {
          markAsComplete()
        } else {
          okButton.text_=(s"$synced / $diffCount")
        }
    }

    dialog.onOpen {
      if (myClient.isConnected) {
        for {
          myId <- myClient.myClientId
          masterId <- myClient.masterClientId
        } myClient.publishEvent(GetWatchingFilesFromPair(myId, masterId))
      }
    }

    configButton.onClick {
      dialogFactories.createWatchFilesDialog(None).showOnCenter()
    }

    cancelButton.onClick {
      dialog.dispose()
    }

    okButton.onClick {
      if (myClient.isConnected) {
        for {
          clientId <- myClient.allClients.map(_.clientId)
          fileSummaries = myClient.watchingFileSummaries
        } myClient.publishEvent(SyncFilesRequest(clientId, fileSummaries))
      }
    }
  }

  private def markAsComplete(): Unit = {
    okButton.text_=("Complete!")

    // FIXME
    // clear all listeners
    okButton.onClick {
      dialog.dispose()
    }
  }

}
