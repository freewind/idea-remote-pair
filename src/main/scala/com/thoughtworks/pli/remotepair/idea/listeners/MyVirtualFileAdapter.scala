package com.thoughtworks.pli.remotepair.idea.listeners

import akka.actor.ActorRef
import com.intellij.openapi.vfs._
import com.thoughtworks.pli.remotepair.core.{MyUtils, PluginLogger}
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.editor_event_handlers._
import com.thoughtworks.pli.remotepair.core.models.MyIde
import com.thoughtworks.pli.remotepair.idea.models.{IdeaFactories, IdeaProjectImpl}

object MyVirtualFileAdapter {
  type Factory = () => MyVirtualFileAdapter
}

// Note: the events here are crossing multiple projects, so we need to check if the related file is inside current project
class MyVirtualFileAdapter(currentProject: IdeaProjectImpl, coreActor: ActorRef, myIde: MyIde, myClient: MyClient, logger: PluginLogger, ideaFactories: IdeaFactories, myUtils: MyUtils)
  extends VirtualFileAdapter {

  private def containsProjectFile(file: VirtualFile): Boolean = myUtils.isSubPath(file.getPath, currentProject.baseDir.path)

  override def fileDeleted(event: VirtualFileEvent) = {
    logger.info("fileDeleted event: " + event)
    if (containsProjectFile(event.getFile)) {
      coreActor ! new EditorFileDeletedEvent(ideaFactories(event.getFile))
    }
  }

  override def fileCreated(event: VirtualFileEvent) = {
    logger.info("fileCreated event: " + event)
    if (containsProjectFile(event.getFile)) {
      coreActor ! new EditorFileCreatedEvent(ideaFactories(event.getFile))
    }
  }

  override def fileMoved(event: VirtualFileMoveEvent) = {
    logger.info(s"fileMoved event: ${event.getOldParent}/${event.getFileName} -> ${event.getFile}")
    if (containsProjectFile(event.getFile)) {
      val oldPath = event.getOldParent.getPath + "/" + event.getFileName
      coreActor ! new EditorFileMovedEvent(ideaFactories(event.getFile), oldPath, event.getNewParent.getPath)
    }
  }

  override def propertyChanged(event: VirtualFilePropertyEvent) = {
    logger.info(s"propertyChanged event: ${event.getPropertyName}: ${event.getOldValue} --> ${event.getNewValue}")
    if (containsProjectFile(event.getFile)) {
      if (event.getPropertyName == VirtualFile.PROP_NAME) {
        coreActor ! new EditorFileRenamedEvent(ideaFactories(event.getFile), event.getOldValue.asInstanceOf[String])
      }
    }
  }

  override def fileCopied(event: VirtualFileCopyEvent) = {
    logger.info("fileCopied event: " + event)
  }

  override def contentsChanged(event: VirtualFileEvent) = {
    logger.info("contentsChanged event: " + event)
  }

}
