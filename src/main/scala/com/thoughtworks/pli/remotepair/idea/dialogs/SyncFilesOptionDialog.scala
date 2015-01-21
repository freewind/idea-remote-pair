package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.{JPanel, JButton}

import com.thoughtworks.pli.intellij.remotepair.protocol.{ClientInfoResponse, GetPairableFilesFromPair, SyncFilesForAll, SyncFilesRequest}
import com.thoughtworks.pli.remotepair.idea.core.{CurrentProjectHolder, PublishEvents, RichProject}

class SyncFilesOptionDialog(override val currentProject: RichProject) extends _SyncFilesOptionDialog with JDialogSupport with CurrentProjectHolder with PublishEvents {

  override def getContentPanel: JPanel = contentPanel

  this.setSize(400, 260)

  if (currentProject.clientInfo.exists(_.isMaster)) {
    for {
      myId <- currentProject.clientInfo.map(_.clientId)
      clients <- currentProject.projectInfo.map(_.clients)
      client <- clients.filterNot(_.clientId == myId)
    } {
      pairClientsToDiff.add(createDiffButton(myId, client))
    }
  } else {
    for {
      myId <- currentProject.clientInfo.map(_.clientId)
      master <- currentProject.projectInfo.flatMap(_.clients.find(_.isMaster))
    } pairClientsToDiff.add(createDiffButton(myId, master))
  }


  private def createDiffButton(myClientId: String, client: ClientInfoResponse): JButton = {
    def masterInfo(client: ClientInfoResponse) = if (client.isMaster) " (master)" else ""
    val button = new JButton()
    button.setText(client.name + masterInfo(client))
    clickOn(button) {
      publishEvent(GetPairableFilesFromPair(myClientId, client.clientId))
    }
    button
  }

  clickOn(btnIgnore) {
    val dialog = new ChooseIgnoreDialog(currentProject)
    dialog.setVisible(true)
  }

  clickOn(okButton) {
    if (currentProject.clientInfo.exists(_.isMaster)) {
      publishEvent(SyncFilesForAll)
    } else {
      for {
        clientId <- currentProject.clientInfo.map(_.clientId)
        ignoredFiles <- currentProject.projectInfo.map(_.ignoredFiles)
        fileSummaries = currentProject.getAllPairableFiles(ignoredFiles).map(currentProject.getFileSummary)
      } publishEvent(SyncFilesRequest(clientId, fileSummaries))
    }
  }

}
