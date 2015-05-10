package com.thoughtworks.pli.remotepair.core.idea_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client.{IsWatching, PublishEvent}
import com.thoughtworks.pli.remotepair.core.models.{MyPlatform, MyProject}
import com.thoughtworks.pli.remotepair.core.{ClientVersionedDocuments, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.file._

class HandleIdeaFileEvent(currentProject: MyProject, myPlatform: MyPlatform, publishEvent: PublishEvent, logger: PluginLogger, isWatching: IsWatching, clientVersionedDocuments: ClientVersionedDocuments, writeToProfileFile: WriteToProjectFile) {
  def handleFileDeleted(event: IdeaFileDeletedEvent): Unit = {
    if (isWatching(event.file)) {
      event.file.relativePath.foreach { path =>
        publishDeleteFile(path, event.file.isDirectory)
      }
    }
  }

  def handleFileCreated(event: IdeaFileCreatedEvent): Unit = {
    if (isWatching(event.file)) {
      event.file.relativePath.foreach { path =>
        val content = if (event.file.isDirectory) None else Some(event.file.content)
        clientVersionedDocuments.find(path) match {
          case Some(doc) => doc.latestContent.foreach(content => writeToProfileFile(path, content))
          case _ => publishCreateFile(path, event.file.isDirectory, content)
        }
      }
    }
  }

  def handleFileMoved(event: IdeaFileMovedEvent): Unit = {
    (currentProject.getRelativePath(event.oldPath), currentProject.getRelativePath(event.newParentPath)) match {
      case (Some(path), Some(newParentPath)) => if (event.file.isDirectory) {
        publishEvent(new MoveDirEvent(path, newParentPath))
      } else {
        publishEvent(new MoveFileEvent(path, newParentPath))
      }
      case _ =>
    }
  }

  def handleFileRenamed(event: IdeaFileRenamedEvent): Unit = {
    val oldPath = event.file.parent.path + "/" + event.oldName
    currentProject.getRelativePath(oldPath) match {
      case Some(old) => if (event.file.isDirectory) {
        publishEvent(RenameDirEvent(old, event.file.name))
      } else {
        publishEvent(RenameFileEvent(old, event.file.name))
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
