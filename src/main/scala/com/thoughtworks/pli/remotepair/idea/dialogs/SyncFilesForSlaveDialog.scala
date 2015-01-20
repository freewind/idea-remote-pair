package com.thoughtworks.pli.remotepair.idea.dialogs

import java.awt.event._

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.{CurrentProjectHolder, RichProject}

class SyncFilesForSlaveDialog(override val currentProject: RichProject)
  extends _SyncFilesForSlaveDialog with CurrentProjectHolder with JDialogSupport with ClientNameGetter {

  @volatile var diffCount: Option[Int] = None
  @volatile var synced: Int = 0

  val monitor: PartialFunction[PairEvent, Any] = {
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

  currentProject.connection.foreach { conn =>
    addWindowListener(new WindowAdapter {
      override def windowOpened(windowEvent: WindowEvent): Unit = {
        currentProject.eventHandler.addReadMonitor(monitor)

        for {
          myId <- currentProject.clientInfo.map(_.clientId)
          clients <- currentProject.projectInfo.map(_.clients)
          client <- clients.filterNot(_.clientId == myId)
        } conn.publish(GetPairableFilesFromPair(myId, client.clientId))
      }
      override def windowClosed(windowEvent: WindowEvent): Unit = {
        currentProject.eventHandler.removeReadMonitor(monitor)
      }
    })
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
