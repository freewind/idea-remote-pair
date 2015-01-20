package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.{CurrentProjectHolder, RichProject}

class SyncFilesForMasterDialog(override val currentProject: RichProject)
  extends _SyncFilesForMasterDialog with JDialogSupport with CurrentProjectHolder with ClientNameGetter {

  init()

  onWindowOpened {
    currentProject.connection.foreach { conn =>
      for {
        myId <- currentProject.myClientId
        otherId <- currentProject.otherClientIds
      } conn.publish(GetPairableFilesFromPair(myId, otherId))
    }
  }

  monitorReadEvent {
    case PairableFiles(ClientName(name), _, fileSummaries) =>
      tabs.addTab(name, fileSummaries, currentProject.getPairableFileSummaries)
    case SyncFilesRequest(ClientName(name), _) => tabs.setMessage(name, "Remote pair is requesting files")
  }

  monitorWrittenEvent {
    case SyncFilesForAll =>
      okButton.setText("Synchronizing ...")
      okButton.setEnabled(false)
    case MasterPairableFiles(_, ClientName(name), _, diffCount) => tabs.setTotalCount(name, diffCount)
    case SyncFileEvent(_, ClientName(name), _, _) => tabs.increase(name)
  }

  clickOn(configButton) {
    new ChooseIgnoreDialog(currentProject).showOnCenter()
  }

  clickOn(okButton) {
    currentProject.connection.foreach { conn =>
      conn.publish(SyncFilesForAll)
    }
  }

}
