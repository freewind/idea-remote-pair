package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs._
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class MyVirtualFileAdapterFactory(invokeLater: InvokeLater, publishEvent: PublishEvent, logger: Logger, containsProjectFile: ContainsProjectFile, getRelativePath: GetRelativePath, getFileContent: GetFileContent, getCachedFileContent: GetCachedFileContent) {

  // Note: the events here are crossing multiple projects, so we need to check if the related file is inside current project
  def create() = new VirtualFileAdapter {

    private def filterForCurrentProject(event: VirtualFileEvent)(f: VirtualFile => Any): Unit = {
      val file = event.getFile
      if (containsProjectFile(file)) f(file)
    }

    override def fileDeleted(event: VirtualFileEvent) = filterForCurrentProject(event) { file =>
      logger.info("### file deleted: " + file)
      getRelativePath(file).foreach { path =>
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
      logger.info("### file created: " + file)
      getRelativePath(file).foreach { path =>
        invokeLater {
          val content = if (file.isDirectory) None else Some(getFileContent(file))
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
      logger.info("### file moved: " + event)
      val isDir = event.getFile.isDirectory

      val newPath = getRelativePath(event.getNewParent.getPath + "/" + event.getFileName)
      newPath.foreach(p => publishCreateFile(p, isDir, if (isDir) None else Some(getFileContent(event.getFile))))

      val oldPath = getRelativePath(event.getOldParent.getPath + "/" + event.getFileName)
      oldPath.foreach(p => publishDeleteFile(p, isDir))
    }

    override def propertyChanged(event: VirtualFilePropertyEvent) = filterForCurrentProject(event) { file =>
      logger.info("### file property changed: " + event)
      logger.info(event.getPropertyName + ": " + event.getOldValue + " ---> " + event.getNewValue)

      // A rename
      if (event.getPropertyName == VirtualFile.PROP_NAME) {
        invokeLater {
          if (event.getFile.isDirectory) {
            val oldPath = event.getFile.getParent.getPath + "/" + event.getOldValue
            getRelativePath(oldPath).foreach(p => publishEvent(DeleteDirEvent(p)))

            val newPath = event.getFile.getParent.getPath + "/" + event.getNewValue
            getRelativePath(newPath).foreach(p => publishEvent(CreateDirEvent(p)))
          } else {
            val oldPath = event.getFile.getParent.getPath + "/" + event.getOldValue
            getRelativePath(oldPath).foreach(p => publishEvent(DeleteFileEvent(p)))

            val newPath = event.getFile.getParent.getPath + "/" + event.getNewValue
            for {
              content <- getCachedFileContent(event.getFile)
              path <- getRelativePath(newPath)
            } publishEvent(CreateFileEvent(path, content))
          }
        }
      }
    }

    override def fileCopied(event: VirtualFileCopyEvent) = filterForCurrentProject(event) { file =>
      logger.info("### file copied: " + file)
    }

    override def contentsChanged(event: VirtualFileEvent) = filterForCurrentProject(event) { file =>
      logger.info("### contents changed: " + file)
    }

  }

}
