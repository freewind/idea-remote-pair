package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JPanel, JButton}

import com.thoughtworks.pli.intellij.remotepair.actions.forms._SyncFilesOptionDialog
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.protocol.{SyncFilesForAll, SyncFilesRequest, ClientInfoResponse, GetPairableFilesFromPair}
import com.thoughtworks.pli.intellij.remotepair.{PublishEvents, RichProject}

class SyncFilesOptionDialog(override val currentProject: RichProject) extends _SyncFilesOptionDialog with JDialogSupport with CurrentProjectHolder with PublishEvents {

  btnIgnore.addActionListener(new ActionListener {
    override def actionPerformed(actionEvent: ActionEvent) {
      showIgnoreDialog()
    }
  })

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

  def showOnCenter(): Unit = {
    this.pack()
    this.setSize(400, 260)
    this.setLocationRelativeTo(currentProject.getWindow())
    this.setVisible(true)
  }

  private def createDiffButton(myClientId: String, client: ClientInfoResponse): JButton = {
    def masterInfo(client: ClientInfoResponse) = if (client.isMaster) " (master)" else ""

    val button = new JButton()
    button.setText(client.name + masterInfo(client))
    button.addActionListener(new ActionListener {
      override def actionPerformed(actionEvent: ActionEvent) = {
        publishEvent(GetPairableFilesFromPair(myClientId, client.clientId))
      }
    })
    button
  }

  private def showIgnoreDialog() = {
    val dialog = new ChooseIgnoreDialog(currentProject)
    dialog.show()
  }
  override def contentPane: JPanel = super.getContentPane
  override def buttonOK: JButton = super.getButtonOK
  override def buttonCancel: JButton = super.getButtonCancel

  override def onCancel(): Unit = ()
  override def onOK(): Unit = {
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
