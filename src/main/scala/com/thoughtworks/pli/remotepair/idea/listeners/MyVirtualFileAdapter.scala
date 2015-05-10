package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.vfs._
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import com.thoughtworks.pli.remotepair.core.client.{IsWatching, PublishEvent}
import com.thoughtworks.pli.remotepair.core.idea_event_handlers._
import com.thoughtworks.pli.remotepair.core.{ClientVersionedDocuments, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.file._
import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl
import com.thoughtworks.pli.remotepair.idea.project.GetProjectBasePath
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

object MyVirtualFileAdapter {
  type Factory = () => MyVirtualFileAdapter
}

// Note: the events here are crossing multiple projects, so we need to check if the related file is inside current project
class MyVirtualFileAdapter(handleIdeaEvent: HandleIdeaEvent, invokeLater: InvokeLater, publishEvent: PublishEvent, logger: PluginLogger, getRelativePath: GetRelativePath, getFileContent: GetFileContent, getCachedFileContent: GetCachedFileContent, isWatching: IsWatching, isDirectory: IsDirectory, clientVersionedDocuments: ClientVersionedDocuments, writeToProfileFile: WriteToProjectFile, getProjectBasePath: GetProjectBasePath, isSubPath: IsSubPath)
  extends VirtualFileAdapter {

  private def containsProjectFile(file: VirtualFile): Boolean = isSubPath(file.getPath, getProjectBasePath())

  override def fileDeleted(event: VirtualFileEvent) = {
    logger.info("fileDeleted event: " + event)
    if (containsProjectFile(event.getFile)) {
      handleIdeaEvent(new IdeaFileDeletedEvent(IdeaFileImpl(event.getFile)))
    }
  }

  override def fileCreated(event: VirtualFileEvent) = {
    logger.info("fileCreated event: " + event)
    if (containsProjectFile(event.getFile)) {
      handleIdeaEvent(new IdeaFileCreatedEvent(IdeaFileImpl(event.getFile)))
    }
  }

  override def fileMoved(event: VirtualFileMoveEvent) = {
    logger.info(s"fileMoved event: ${event.getOldParent}/${event.getFileName} -> ${event.getFile}")
    if (containsProjectFile(event.getFile)) {
      val oldPath = event.getOldParent.getPath + "/" + event.getFileName
      handleIdeaEvent(new IdeaFileMovedEvent(IdeaFileImpl(event.getFile), oldPath, event.getNewParent.getPath))
    }
  }

  override def propertyChanged(event: VirtualFilePropertyEvent) = {
    logger.info(s"propertyChanged event: ${event.getPropertyName}: ${event.getOldValue} --> ${event.getNewValue}")
    if (containsProjectFile(event.getFile)) {
      if (event.getPropertyName == VirtualFile.PROP_NAME) {
        handleIdeaEvent(new IdeaFileRenamedEvent(IdeaFileImpl(event.getFile), event.getOldValue.asInstanceOf[String]))
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
