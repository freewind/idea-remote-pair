package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs._
import com.thoughtworks.pli.intellij.remotepair.protocol._

// Note: the events here are crossing multiple projects, so we need to check if the related file is inside current project
class MyVirtualFileAdapter(override val currentProject: RichProject) extends VirtualFileAdapter with PublishEvents with InvokeLater with CurrentProjectHolder with AppLogger {

  private def filterForCurrentProject(event: VirtualFileEvent)(f: VirtualFile => Any): Unit = {
    val file = event.getFile
    if (currentProject.containsFile(file)) f(file)
  }

  override def fileDeleted(event: VirtualFileEvent) = filterForCurrentProject(event) { file =>
    log.info("### file deleted: " + file)
    currentProject.getRelativePath(file).foreach { path =>
      invokeLater {
        publishDeleteFile(path, file.isDirectory)
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
    log.info("### file created: " + file)
    currentProject.getRelativePath(file).foreach { path =>
      invokeLater {
        val content = if (file.isDirectory) None else Some(currentProject.getFileContent(file))
        publishCreateFile(path, file.isDirectory, content)
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

  override def fileMoved(event: VirtualFileMoveEvent) = {
    log.info("### file moved: " + event)
    val isDir = event.getFile.isDirectory

    val newPath = currentProject.getRelativePath(event.getNewParent.getPath + "/" + event.getFileName)
    newPath.foreach(p => publishCreateFile(p, isDir, if (isDir) None else Some(currentProject.getFileContent(event.getFile))))

    val oldPath = currentProject.getRelativePath(event.getOldParent.getPath + "/" + event.getFileName)
    oldPath.foreach(p => publishDeleteFile(p, isDir))
  }

  override def propertyChanged(event: VirtualFilePropertyEvent) = filterForCurrentProject(event) { file =>
    log.info("### file property changed: " + event)
    log.info(event.getPropertyName + ": " + event.getOldValue + " ---> " + event.getNewValue)

    // A rename
    if (event.getPropertyName == VirtualFile.PROP_NAME) {
      invokeLater {
        if (event.getFile.isDirectory) {
          val oldPath = event.getFile.getParent.getPath + "/" + event.getOldValue
          currentProject.getRelativePath(oldPath).foreach(p => publishEvent(DeleteDirEvent(p)))

          val newPath = event.getFile.getParent.getPath + "/" + event.getNewValue
          currentProject.getRelativePath(newPath).foreach(p => publishEvent(CreateDirEvent(p)))
        } else {
          val oldPath = event.getFile.getParent.getPath + "/" + event.getOldValue
          currentProject.getRelativePath(oldPath).foreach(p => publishEvent(DeleteFileEvent(p)))

          val newPath = event.getFile.getParent.getPath + "/" + event.getNewValue
          for {
            content <- currentProject.getCachedFileContent(event.getFile)
            path <- currentProject.getRelativePath(newPath)
          } publishEvent(CreateFileEvent(path, content))
        }
      }
    }
  }

  override def fileCopied(event: VirtualFileCopyEvent) = filterForCurrentProject(event) { file =>
    log.info("### file copied: " + file)
  }

  override def contentsChanged(event: VirtualFileEvent) = filterForCurrentProject(event) { file =>
    log.info("### contents changed: " + file)
  }

}
