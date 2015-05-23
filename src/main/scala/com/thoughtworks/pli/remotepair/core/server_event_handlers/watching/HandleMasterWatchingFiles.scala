package com.thoughtworks.pli.remotepair.core.server_event_handlers.watching

import com.thoughtworks.pli.intellij.remotepair.protocol.MasterWatchingFiles
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.MyIde

class HandleMasterWatchingFiles(myClient: MyClient, myPlatform: MyIde, logger: PluginLogger) {

  def apply(event: MasterWatchingFiles): Unit = myPlatform.invokeLater {
    if (event.paths.nonEmpty) {
      myClient.allWatchingFiles.foreach { myFile =>
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
