package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.intellij.remotepair.protocol.MasterWatchingFiles
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.utils.{InvokeLater, RunWriteAction}

case class HandleMasterWatchingFiles(currentProject: RichProject, invokeLater: InvokeLater, runWriteAction: RunWriteAction, logger: Logger) {
  def apply(event: MasterWatchingFiles): Unit = {
    val ignoredFiles = currentProject.watchingFiles
    invokeLater {
      if (event.paths.nonEmpty) {
        currentProject.getAllWatchingiles(ignoredFiles).foreach { myFile =>
          if (!event.paths.contains(currentProject.getRelativePath(myFile))) {
            logger.info("#### delete file which is not exist on master side: " + myFile.getPath)
            if (myFile.exists()) {
              runWriteAction(myFile.delete(this))
            }
          }
        }
      }
    }
  }

}
