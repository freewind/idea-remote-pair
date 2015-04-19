package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.JButton

import com.thoughtworks.pli.intellij.remotepair.protocol.{ClientInfoResponse, GetWatchingFilesFromPair, SyncFilesForAll, SyncFilesRequest}
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

class AmIMaster(clientInfoHolder: ClientInfoHolder) {
  def apply(): Boolean = clientInfoHolder.get.exists(_.isMaster)
}

case class SyncFilesOptionDialog(chooseIgnoreDialogFactory: WatchFilesDialog.Factory, publishEvent: PublishEvent, invokeLater: InvokeLater, pairEventListeners: PairEventListeners, clientInfoHolder: ClientInfoHolder, amIMaster: AmIMaster, getMyClientId: GetMyClientId, getOtherClients: GetOtherClients, getMasterClient: GetMasterClient, getWatchingFileSummaries: GetWatchingFileSummaries, getProjectWindow: GetProjectWindow)
  extends _SyncFilesOptionDialog with JDialogSupport {

  this.setSize(Size(400, 260))

  if (amIMaster()) {
    for {
      myId <- getMyClientId()
      client <- getOtherClients()
    } {
      pairClientsToDiffPanel.add(createDiffButton(myId, client))
    }
  } else {
    for {
      myId <- getMyClientId()
      master <- getMasterClient()
    } pairClientsToDiffPanel.add(createDiffButton(myId, master))
  }

  private def createDiffButton(myClientId: String, client: ClientInfoResponse): JButton = {
    def masterInfo(client: ClientInfoResponse) = if (client.isMaster) " (master)" else ""
    val button = new JButton()
    button.setText(client.name + masterInfo(client))
    onClick(button) {
      publishEvent(GetWatchingFilesFromPair(myClientId, client.clientId))
    }
    button
  }

  onClick(configButton) {
    val dialog = chooseIgnoreDialogFactory(None)
    dialog.setVisible(true)
  }

  onClick(okButton) {
    if (amIMaster()) {
      publishEvent(SyncFilesForAll)
    } else {
      for {
        clientId <- getMyClientId()
        fileSummaries = getWatchingFileSummaries()
      } publishEvent(SyncFilesRequest(clientId, fileSummaries))
    }
  }

}
