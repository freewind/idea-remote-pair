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

case class SyncFilesForMasterDialog(currentProject: MyProject, myIde: MyIde, myClient: MyClient, dialogFactories: DialogFactories, pairEventListeners: PairEventListeners)
  extends _SyncFilesBaseDialog with JDialogSupport with BaseVirtualDialog {

  init()

  override def init(): Unit = {
    this.onOpen {
      if (myClient.isConnected) {
        for {
          myId <- myClient.myClientId
          otherId <- myClient.otherClients.map(_.clientId)
        } myClient.publishEvent(GetWatchingFilesFromPair(myId, otherId))
      }
    }

    monitorReadEvent {
      case WatchingFiles(fromClientId, _, fileSummaries) => myClient.clientIdToName(fromClientId).foreach { name =>
        _tabs.addTab(name, fileSummaries, myClient.watchingFileSummaries)
      }
      case SyncFilesRequest(fromClientId, _) => myClient.clientIdToName(fromClientId).foreach { name =>
        _tabs.setMessage(name, "Remote pair is requesting files")
      }
    }

    monitorWrittenEvent {
      case SyncFilesForAll =>
        _okButton.text_=("Synchronizing ...")
        _okButton.enabled = false
      case MasterWatchingFiles(_, toClientId, _, diffCount) => myClient.clientIdToName(toClientId).foreach(name => _tabs.setTotalCount(name, diffCount))
      case SyncFileEvent(_, toClientId, _, _) => myClient.clientIdToName(toClientId).foreach(name => _tabs.increase(name))
    }

    _configButton.onClick {
      dialogFactories.createWatchFilesDialog(None).showOnCenter()
    }

    _cancelButton.onClick {
      this.dispose()
    }

    _okButton.onClick {
      myClient.publishEvent(SyncFilesForAll)
    }
  }
}
