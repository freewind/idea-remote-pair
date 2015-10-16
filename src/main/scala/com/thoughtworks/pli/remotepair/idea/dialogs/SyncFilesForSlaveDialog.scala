package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.JDialog

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.core.ui.dialogs.BaseVirtualDialog
import com.thoughtworks.pli.remotepair.idea.dialogs.SwingVirtualImplicits._
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

import scala.language.reflectiveCalls

case class SyncFilesForSlaveDialog(currentProject: MyProject, myClient: MyClient, dialogFactories: DialogFactories, myIde: MyIde, pairEventListeners: PairEventListeners)
  extends _SyncFilesBaseDialog with JDialogSupport with BaseVirtualDialog {

  init()

  @volatile var diffCount: Option[Int] = None
  @volatile var synced: Int = 0

  override def init(): Unit = {
    monitorReadEvent {
      case WatchingFiles(fromClientId, _, fileSummaries) => myClient.clientIdToName(fromClientId).foreach { name =>
        _tabs.addTab(name, myClient.watchingFileSummaries, fileSummaries)
      }
      case MasterWatchingFiles(_, _, _, diff) =>
        if (diff == 0) {
          markAsComplete()
        } else {
          diffCount = Some(diff)
          _okButton.text_=(s"$synced / $diffCount")
        }
      case event: SyncFileEvent =>
        synced += 1
        if (Some(synced) == diffCount) {
          markAsComplete()
        } else {
          _okButton.text_=(s"$synced / $diffCount")
        }
    }

    this.onOpen {
      if (myClient.isConnected) {
        for {
          myId <- myClient.myClientId
          masterId <- myClient.masterClientId
        } myClient.publishEvent(GetWatchingFilesFromPair(myId, masterId))
      }
    }

    _configButton.onClick {
      dialogFactories.createWatchFilesDialog(None).showOnCenter()
    }

    _cancelButton.onClick {
      this.dispose()
    }

    _okButton.onClick {
      if (myClient.isConnected) {
        for {
          clientId <- myClient.allClients.map(_.clientId)
          fileSummaries = myClient.watchingFileSummaries
        } myClient.publishEvent(SyncFilesRequest(clientId, fileSummaries))
      }
    }
  }

  private def markAsComplete(): Unit = {
    _okButton.text_=("Complete!")

    // FIXME
    // clear all listeners
    _okButton.onClick {
      this.dispose()
    }
  }

}

