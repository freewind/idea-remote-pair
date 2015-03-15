package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.JButton

import com.thoughtworks.pli.intellij.remotepair.protocol.{ClientInfoResponse, GetPairableFilesFromPair, SyncFilesForAll, SyncFilesRequest}
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.{PairEventListeners, PublishEvent}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class SyncFilesOptionDialog(currentProject: RichProject, chooseIgnoreDialogFactory: ChooseIgnoreDialogFactory, publishEvent: PublishEvent, invokeLater: InvokeLater, pairEventListeners: PairEventListeners)
  extends _SyncFilesOptionDialog with JDialogSupport {

  this.setSize(Size(400, 260))

  if (currentProject.clientInfo.exists(_.isMaster)) {
    for {
      myId <- currentProject.clientInfo.map(_.clientId)
      clients <- currentProject.projectInfo.map(_.clients)
      client <- clients.filterNot(_.clientId == myId)
    } {
      pairClientsToDiffPanel.add(createDiffButton(myId, client))
    }
  } else {
    for {
      myId <- currentProject.clientInfo.map(_.clientId)
      master <- currentProject.projectInfo.flatMap(_.clients.find(_.isMaster))
    } pairClientsToDiffPanel.add(createDiffButton(myId, master))
  }

  private def createDiffButton(myClientId: String, client: ClientInfoResponse): JButton = {
    def masterInfo(client: ClientInfoResponse) = if (client.isMaster) " (master)" else ""
    val button = new JButton()
    button.setText(client.name + masterInfo(client))
    onClick(button) {
      publishEvent(GetPairableFilesFromPair(myClientId, client.clientId))
    }
    button
  }

  onClick(configButton) {
    val dialog = chooseIgnoreDialogFactory.create()
    dialog.setVisible(true)
  }

  onClick(okButton) {
    if (currentProject.clientInfo.exists(_.isMaster)) {
      publishEvent(SyncFilesForAll)
    } else {
      for {
        clientId <- currentProject.clientInfo.map(_.clientId)
        ignoredFiles <- currentProject.projectInfo.map(_.ignoredFiles)
        fileSummaries = currentProject.getAllPairableFiles(ignoredFiles).flatMap(currentProject.getFileSummary)
      } publishEvent(SyncFilesRequest(clientId, fileSummaries))
    }
  }

}
