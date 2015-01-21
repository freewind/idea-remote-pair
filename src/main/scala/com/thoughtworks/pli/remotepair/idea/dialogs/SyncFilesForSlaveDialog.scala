package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.JPanel

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.{CurrentProjectHolder, RichProject}

class SyncFilesForSlaveDialog(override val currentProject: RichProject)
  extends _SyncFilesForSlaveDialog with CurrentProjectHolder with JDialogSupport with ClientNameGetter {

  @volatile var diffCount: Option[Int] = None
  @volatile var synced: Int = 0

  override def getContentPanel: JPanel = contentPane

  monitorReadEvent {
    case MasterPairableFiles(_, _, _, diff) =>
      diffCount = Some(diff)
      okButton.setText(s"$synced / $diffCount")
    case event: SyncFileEvent =>
      synced += 1
      if (Some(synced) == diffCount) {
        okButton.setText("Complete!")
      } else {
        okButton.setText(s"$synced / $diffCount")
      }
    case PairableFiles(ClientName(name), _, fileSummaries) =>
      tabs.addTab(name, currentProject.getPairableFileSummaries, fileSummaries)
  }

  onWindowOpened {
    for {
      conn <- currentProject.connection
      myId <- currentProject.clientInfo.map(_.clientId)
      clients <- currentProject.projectInfo.map(_.clients)
      client <- clients.filterNot(_.clientId == myId)
    } conn.publish(GetPairableFilesFromPair(myId, client.clientId))
  }


  clickOn(configButton) {
    new ChooseIgnoreDialog(currentProject).setVisible(true)
  }

  clickOn(okButton) {
    for {
      conn <- currentProject.connection
      clientId <- currentProject.clientInfo.map(_.clientId)
      ignoredFiles <- currentProject.projectInfo.map(_.ignoredFiles)
      fileSummaries = currentProject.getAllPairableFiles(ignoredFiles).map(currentProject.getFileSummary)
    } conn.publish(SyncFilesRequest(clientId, fileSummaries))
  }

}
