package com.thoughtworks.pli.remotepair.core.idea_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client.{IsWatching, PublishEvent}
import com.thoughtworks.pli.remotepair.core.{ClientVersionedDocuments, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.file._
import com.thoughtworks.pli.remotepair.idea.project.ContainsProjectFile
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

class HandleIdeaFileEvent(invokeLater: InvokeLater, publishEvent: PublishEvent, logger: PluginLogger, containsProjectFile: ContainsProjectFile, getRelativePath: GetRelativePath, getFileContent: GetFileContent, getCachedFileContent: GetCachedFileContent, isWatching: IsWatching, isDirectory: IsDirectory, clientVersionedDocuments: ClientVersionedDocuments, writeToProfileFile: WriteToProjectFile) {
  def handleFileDeleted(event: IdeaFileDeletedEvent): Unit = {
    if (isWatching(event.file)) {
      getRelativePath(event.file).foreach { path =>
        publishDeleteFile(path, isDirectory(event.file))
      }
    }
  }

  def handleFileCreated(event: IdeaFileCreatedEvent): Unit = {
    if (isWatching(event.file)) {
      getRelativePath(event.file).foreach { path =>
        val content = if (isDirectory(event.file)) None else Some(getFileContent(event.file))
        clientVersionedDocuments.find(path) match {
          case Some(doc) => doc.latestContent.foreach(content => writeToProfileFile(path, content))
          case _ => publishCreateFile(path, isDirectory(event.file), content)
        }
      }
    }
  }

  def handleFileMoved(event: IdeaFileMovedEvent): Unit = {
    (getRelativePath(event.oldPath), getRelativePath(event.newParentPath)) match {
      case (Some(path), Some(newParentPath)) => if (isDirectory(event.file)) {
        publishEvent(new MoveDirEvent(path, newParentPath))
      } else {
        publishEvent(new MoveFileEvent(path, newParentPath))
      }
      case _ =>
    }
  }

  def handleFileRenamed(event: IdeaFileRenamedEvent): Unit = {
    val oldPath = event.file.getParent.getPath + "/" + event.oldName
    getRelativePath(oldPath) match {
      case Some(old) => if (event.file.isDirectory) {
        publishEvent(RenameDirEvent(old, event.file.getName))
      } else {
        publishEvent(RenameFileEvent(old, event.file.getName))
      }
      case _ =>
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

  private def publishCreateFile(relativePath: String, isDirectory: Boolean, content: Option[Content]) {
    val createdEvent = if (isDirectory) {
      CreateDirEvent(relativePath)
    } else {
      CreateFileEvent(relativePath, content.get)
    }
    publishEvent(createdEvent)
  }

}
