package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient
import com.thoughtworks.pli.remotepair.core.models.{MyPlatform, MyProject}
import com.thoughtworks.pli.remotepair.core.{ClientVersionedDocuments, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.file._

class HandleIdeaFileEvent(currentProject: MyProject, myPlatform: MyPlatform, connectedClient: ConnectedClient, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments, writeToProjectFile: WriteToProjectFile) {
  def handleFileDeleted(event: EditorFileDeletedEvent): Unit = {
    if (connectedClient.isWatching(event.file)) {
      event.file.relativePath.foreach { path =>
        publishDeleteFile(path, event.file.isDirectory)
      }
    }
  }

  def handleFileCreated(event: EditorFileCreatedEvent): Unit = {
    if (connectedClient.isWatching(event.file)) {
      event.file.relativePath.foreach { path =>
        val content = if (event.file.isDirectory) None else Some(event.file.content)
        clientVersionedDocuments.find(path) match {
          case Some(doc) => doc.latestContent.foreach(content => writeToProjectFile(path, content))
          case _ => publishCreateFile(path, event.file.isDirectory, content)
        }
      }
    }
  }

  def handleFileMoved(event: EditorFileMovedEvent): Unit = {
    (currentProject.getRelativePath(event.oldPath), currentProject.getRelativePath(event.newParentPath)) match {
      case (Some(path), Some(newParentPath)) => if (event.file.isDirectory) {
        connectedClient.publishEvent(new MoveDirEvent(path, newParentPath))
      } else {
        connectedClient.publishEvent(new MoveFileEvent(path, newParentPath))
      }
      case _ =>
    }
  }

  def handleFileRenamed(event: EditorFileRenamedEvent): Unit = {
    val oldPath = event.file.parent.path + "/" + event.oldName
    currentProject.getRelativePath(oldPath) match {
      case Some(old) => if (event.file.isDirectory) {
        connectedClient.publishEvent(RenameDirEvent(old, event.file.name))
      } else {
        connectedClient.publishEvent(RenameFileEvent(old, event.file.name))
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
    connectedClient.publishEvent(deleteEvent)
  }

  private def publishCreateFile(relativePath: String, isDirectory: Boolean, content: Option[Content]) {
    val createdEvent = if (isDirectory) {
      CreateDirEvent(relativePath)
    } else {
      CreateFileEvent(relativePath, content.get)
    }
    connectedClient.publishEvent(createdEvent)
  }

}
