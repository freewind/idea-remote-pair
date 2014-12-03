package com.thoughtworks.pli.intellij.remotepair

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
  }

  override def fileCreated(event: VirtualFileEvent) = filterForCurrentProject(event) { file =>
    println("### file created: " + file + ", current base dir: " + currentProject.getBaseDir)
    invokeLater {
      val relativePath = currentProject.getRelativePath(file)
      val createdEvent = if (file.isDirectory) {
        CreateDirEvent(relativePath)
      } else {
        CreateFileEvent(relativePath, currentProject.getContentAsString(file))
      }
      publishEvent(createdEvent)
    }
  }

  override def fileMoved(event: VirtualFileMoveEvent) = filterForCurrentProject(event) { file =>
    println("### file moved: " + file)
  }

  override def propertyChanged(event: VirtualFilePropertyEvent) = filterForCurrentProject(event) { file =>
    println("### file property changed: " + file)
  }

  override def fileCopied(event: VirtualFileCopyEvent) = filterForCurrentProject(event) { file =>
    println("### file copied: " + file)
  }

  override def contentsChanged(event: VirtualFileEvent) = filterForCurrentProject(event) { file =>
    println("### contents changed: " + file)
  }

}
