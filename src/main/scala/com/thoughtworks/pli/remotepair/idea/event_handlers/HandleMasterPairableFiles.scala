package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.intellij.remotepair.protocol.MasterPairableFiles
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.utils.{InvokeLater, RunWriteAction}

case class HandleMasterPairableFiles(currentProject: RichProject, invokeLater: InvokeLater, runWriteAction: RunWriteAction, logger: Logger) {
  def apply(event: MasterPairableFiles): Unit = {
    val ignoredFiles = currentProject.ignoredFiles
    invokeLater {
      if (event.paths.nonEmpty) {
        currentProject.getAllPairableFiles(ignoredFiles).foreach { myFile =>
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
