package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs._
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.files.IsDirectory
import com.thoughtworks.pli.remotepair.idea.core.models.myfile.WriteToProjectFile
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

object MyVirtualFileAdapter {
  type Factory = () => MyVirtualFileAdapter
}

// Note: the events here are crossing multiple projects, so we need to check if the related file is inside current project
class MyVirtualFileAdapter(invokeLater: InvokeLater, publishEvent: PublishEvent, logger: PluginLogger, containsProjectFile: ContainsProjectFile, getRelativePath: GetRelativePath, getFileContent: GetFileContent, getCachedFileContent: GetCachedFileContent, isWatching: IsWatching, isDirectory: IsDirectory, clientVersionedDocuments: ClientVersionedDocuments, writeToProfileFile: WriteToProjectFile)
  extends VirtualFileAdapter {

  private def filterForCurrentProject(event: VirtualFileEvent)(f: VirtualFile => Any): Unit = {
    val file = event.getFile
    if (containsProjectFile(file)) f(file)
  }

  override def fileDeleted(event: VirtualFileEvent) = filterForCurrentProject(event) { file =>
    logger.info("fileDeleted event: " + file)
    if (isWatching(file)) {
      getRelativePath(file).foreach { path =>
        publishDeleteFile(path, isDirectory(file))
      }
    }
  }

  private def publishDeleteFile(relativePath: String, isDir: Boolean) = {
    val deleteEvent = if (isDir) {
      DeleteDirEvent(relativePath)
    } else {
      DeleteFileEvent(relativePath)
    }
    publishEvent(deleteEvent)
  }

  override def fileCreated(event: VirtualFileEvent) = filterForCurrentProject(event) { file =>
    logger.info("fileCreated event: " + file)
    if (isWatching(file)) {
      getRelativePath(file).foreach { path =>
        val content = if (isDirectory(file)) None else Some(getFileContent(file))
        clientVersionedDocuments.find(path) match {
          case Some(doc) => doc.latestContent.foreach(content => writeToProfileFile(path, content))
          case _ => publishCreateFile(path, isDirectory(file), content)
        }

      }
    }
  }

  private def publishCreateFile(relativePath: String, isDirectory: Boolean, content: Option[Content]) {
    val createdEvent = if (isDirectory) {
      CreateDirEvent(relativePath)
    } else {
      CreateFileEvent(relativePath, content.get)
    }
    publishEvent(createdEvent)
  }

  override def fileMoved(event: VirtualFileMoveEvent) = filterForCurrentProject(event) { file =>
    logger.info(s"fileMoved event: ${event.getOldParent}/${event.getFileName} -> ${event.getFile}")
    (getRelativePath(event.getOldParent.getPath + "/" + event.getFileName), getRelativePath(event.getNewParent)) match {
      case (Some(path), Some(newParentPath)) => if (isDirectory(event.getFile)) {
        publishEvent(new MoveDirEvent(path, newParentPath))
      } else {
        publishEvent(new MoveFileEvent(path, newParentPath))
      }
      case _ =>
    }
  }

  override def propertyChanged(event: VirtualFilePropertyEvent) = filterForCurrentProject(event) { file =>
    logger.info(s"propertyChanged event: ${event.getPropertyName}: ${event.getOldValue} --> ${event.getNewValue}")

    // A rename
    if (event.getPropertyName == VirtualFile.PROP_NAME) {
      val oldPath = event.getFile.getParent.getPath + "/" + event.getOldValue
      getRelativePath(oldPath) match {
        case Some(old) => if (event.getFile.isDirectory) {
          publishEvent(RenameDirEvent(old, event.getNewValue.toString))
        } else {
          publishEvent(RenameFileEvent(old, event.getNewValue.toString))
        }
        case _ =>
      }
    }
  }

  override def fileCopied(event: VirtualFileCopyEvent) = filterForCurrentProject(event) { file =>
    logger.info("fileCopied event: " + file)
  }

  override def contentsChanged(event: VirtualFileEvent) = filterForCurrentProject(event) { file =>
    logger.info("contentsChanged event: " + file + ", event: " + event)
  }

}
