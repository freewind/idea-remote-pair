package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.MyIde
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

import scala.language.reflectiveCalls

object SyncFilesForSlaveDialog {
  type Factory = () => SyncFilesForSlaveDialog
}

trait MySyncFilesForSlaveDialog extends MyWindow {
  def currentProject: IdeaProjectImpl
  def myClient: MyClient
  def watchFilesDialogFactory: WatchFilesDialog.Factory
  def myIde: MyIde
  def pairEventListeners: PairEventListeners

  @volatile var diffCount: Option[Int] = None
  @volatile var synced: Int = 0

  val okButton: VirtualButton
  val configButton: VirtualButton
  val cancelButton: VirtualButton
  val tabs: {
    def addTab(name: String, leftFileSummaries: Seq[FileSummary], rightFileSummaries: Seq[FileSummary]): Unit
  }

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
    watchFilesDialogFactory(None).showOnCenter()
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

  private def markAsComplete(): Unit = {
    okButton.text_=("Complete!")

    // FIXME
    // clear all listeners
    okButton.onClick {
      dialog.dispose()
    }
  }

}
case class SyncFilesForSlaveDialog(currentProject: IdeaProjectImpl, myClient: MyClient, watchFilesDialogFactory: WatchFilesDialog.Factory, myIde: MyIde, pairEventListeners: PairEventListeners)
  extends _SyncFilesBaseDialog with JDialogSupport with MySyncFilesForSlaveDialog {

  import SwingVirtualImplicits._

  override val dialog: VirtualDialog = this
  override val okButton: VirtualButton = _okButton
  override val cancelButton: VirtualButton = _cancelButton
  override val configButton: VirtualButton = _configButton
  override val tabs = _tabs

}

