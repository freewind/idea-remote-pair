package com.thoughtworks.pli.remotepair.core.server_event_handlers.watching

import com.thoughtworks.pli.intellij.remotepair.protocol.MasterWatchingFiles
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.GetAllWatchingFiles
import com.thoughtworks.pli.remotepair.idea.file.{DeleteFile, FileExists, GetFilePath, GetRelativePath}
import com.thoughtworks.pli.remotepair.idea.utils.{InvokeLater, RunWriteAction}

case class HandleMasterWatchingFiles(getRelativePath: GetRelativePath, getAllWatchingFiles: GetAllWatchingFiles, invokeLater: InvokeLater, runWriteAction: RunWriteAction, logger: PluginLogger, deleteFile: DeleteFile, fileExists: FileExists, getFilePath: GetFilePath) {

  def apply(event: MasterWatchingFiles): Unit = invokeLater {
    if (event.paths.nonEmpty) {
      getAllWatchingFiles().foreach { myFile =>
        getRelativePath(myFile) match {
          case Some(path) if !event.paths.contains(path) && fileExists(myFile) =>
            logger.info("Delete file which is not exist on master side: " + getFilePath(myFile))
            runWriteAction(deleteFile(myFile))
          case _ =>
        }
      }
    }
  }

}
