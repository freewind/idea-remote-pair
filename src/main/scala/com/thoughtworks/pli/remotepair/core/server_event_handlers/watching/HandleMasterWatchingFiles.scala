package com.thoughtworks.pli.remotepair.core.server_event_handlers.watching

import com.thoughtworks.pli.intellij.remotepair.protocol.MasterWatchingFiles
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.GetAllWatchingFiles
import com.thoughtworks.pli.remotepair.core.models.MyPlatform

class HandleMasterWatchingFiles(getAllWatchingFiles: GetAllWatchingFiles, myPlatform: MyPlatform, logger: PluginLogger) {

  def apply(event: MasterWatchingFiles): Unit = myPlatform.invokeLater {
    if (event.paths.nonEmpty) {
      getAllWatchingFiles().foreach { myFile =>
        myFile.relativePath match {
          case Some(path) if !event.paths.contains(path) && myFile.exists =>
            logger.info("Delete file which is not exist on master side: " + myFile.path)
            myPlatform.runWriteAction(myFile.delete())
          case _ =>
        }
      }
    }
  }

}
