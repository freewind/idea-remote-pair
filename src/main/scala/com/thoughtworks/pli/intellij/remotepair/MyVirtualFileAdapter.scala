package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs._
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder

// Note: the events here are crossing multiple projects, so we need to check if the related file is inside current project
class MyVirtualFileAdapter(override val currentProject: RichProject) extends VirtualFileAdapter with PublishEvents with InvokeLater with CurrentProjectHolder {

  private def filterForCurrentProject(event: VirtualFileEvent)(f: VirtualFile => Any): Unit = {
    val file = event.getFile
    if (currentProject.containsFile(file)) f(file)
  }

  override def fileDeleted(event: VirtualFileEvent) = filterForCurrentProject(event) { file =>
    println("### file deleted: " + file)
    invokeLater {
      publishDeleteFile(currentProject.getRelativePath(file), file.isDirectory)
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
    println("### file created: " + file)
    invokeLater {
      val content = if (file.isDirectory) None else Some(currentProject.getContentAsString(file))
      publishCreateFile(currentProject.getRelativePath(file), file.isDirectory, content)
    }
  }

  private def publishCreateFile(relativePath: String, isDirectory: Boolean, content: Option[String]) {
    val createdEvent = if (isDirectory) {
      CreateDirEvent(relativePath)
    } else {
      CreateFileEvent(relativePath, content.get)
    }
    publishEvent(createdEvent)
  }

  override def fileMoved(event: VirtualFileMoveEvent) = {
    println("### file moved: " + event)
    val isDir = event.getFile.isDirectory

    val newPath = currentProject.getRelativePath(event.getNewParent.getPath + "/" + event.getFileName)
    publishCreateFile(newPath, isDir, if (isDir) None else Some(currentProject.getContentAsString(event.getFile)))

    val oldPath = currentProject.getRelativePath(event.getOldParent.getPath + "/" + event.getFileName)
    publishDeleteFile(oldPath, isDir)
  }

  override def propertyChanged(event: VirtualFilePropertyEvent) = filterForCurrentProject(event) { file =>
    println("### file property changed: " + event)
    println(event.getPropertyName + ": " + event.getOldValue + " ---> " + event.getNewValue)

    // A rename
    if (event.getPropertyName == VirtualFile.PROP_NAME) {
      invokeLater {
        if (event.getFile.isDirectory) {
          val oldPath = event.getFile.getParent.getPath + "/" + event.getOldValue
          publishEvent(DeleteDirEvent(currentProject.getRelativePath(oldPath)))

          val newPath = event.getFile.getParent.getPath + "/" + event.getNewValue
          publishEvent(CreateDirEvent(currentProject.getRelativePath(newPath)))
        } else {
          val oldPath = event.getFile.getParent.getPath + "/" + event.getOldValue
          publishEvent(DeleteFileEvent(currentProject.getRelativePath(oldPath)))

          val newPath = event.getFile.getParent.getPath + "/" + event.getNewValue
          val content = FileDocumentManager.getInstance().getCachedDocument(event.getFile).getCharsSequence.toString
          publishEvent(CreateFileEvent(currentProject.getRelativePath(newPath), content))
        }
      }
    }
  }

  override def fileCopied(event: VirtualFileCopyEvent) = filterForCurrentProject(event) { file =>
    println("### file copied: " + file)
  }

  override def contentsChanged(event: VirtualFileEvent) = filterForCurrentProject(event) { file =>
    println("### contents changed: " + file)
  }

}
