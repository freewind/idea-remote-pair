package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.intellij.remotepair.protocol.MasterWatchingFiles
import com.thoughtworks.pli.remotepair.idea.core.{GetRelativePath, GetAllWatchingFiles}
import com.thoughtworks.pli.remotepair.idea.utils.{InvokeLater, RunWriteAction}

case class HandleMasterWatchingFiles(getRelativePath: GetRelativePath, getAllWatchingFiles: GetAllWatchingFiles, invokeLater: InvokeLater, runWriteAction: RunWriteAction, logger: Logger) {
  def apply(event: MasterWatchingFiles): Unit = {
    invokeLater {
      if (event.paths.nonEmpty) {
        getAllWatchingFiles().foreach { myFile =>
          if (!event.paths.contains(getRelativePath(myFile))) {
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
