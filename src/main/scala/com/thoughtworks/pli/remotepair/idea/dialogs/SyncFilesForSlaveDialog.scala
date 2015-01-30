package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.{CurrentProjectHolder, RichProject}

class SyncFilesForSlaveDialog(override val currentProject: RichProject)
  extends _SyncFilesBaseDialog with CurrentProjectHolder with JDialogSupport with ClientNameGetter {

  @volatile var diffCount: Option[Int] = None
  @volatile var synced: Int = 0

  monitorReadEvent {
    case PairableFiles(ClientName(name), _, fileSummaries) =>
      tabs.addTab(name, currentProject.getPairableFileSummaries, fileSummaries)
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
  }

  onWindowOpened {
    for {
      conn <- currentProject.connection
      myId <- currentProject.myClientId
      masterId <- currentProject.masterClientId
    } conn.publish(GetPairableFilesFromPair(myId, masterId))
  }

  clickOn(configButton) {
    new ChooseIgnoreDialog(currentProject).showOnCenter()
  }

  clickOn(cancelButton) {
    dispose()
  }

  clickOn(okButton) {
    for {
      conn <- currentProject.connection
      clientId <- currentProject.clientInfo.map(_.clientId)
      ignoredFiles <- currentProject.projectInfo.map(_.ignoredFiles)
      fileSummaries = currentProject.getAllPairableFiles(ignoredFiles).flatMap(currentProject.getFileSummary)
    } conn.publish(SyncFilesRequest(clientId, fileSummaries))
  }

}
