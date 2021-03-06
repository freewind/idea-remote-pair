package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.ClientVersionedDocuments

class HandleIdeaFileEvent(currentProject: MyProject, myIde: MyIde, myClient: MyClient, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments) {
  def handleFileDeleted(event: EditorFileDeletedEvent): Unit = {
    if (myClient.isWatching(event.file)) {
      event.file.relativePath.foreach { path =>
        publishDeleteFile(path, event.file.isDirectory)
      }
    }
  }

  def handleFileCreated(event: EditorFileCreatedEvent): Unit = {
    if (myClient.isWatching(event.file)) {
      event.file.relativePath.foreach { path =>
        val content = if (event.file.isDirectory) None else Some(event.file.content)
        clientVersionedDocuments.find(path) match {
          case Some(doc) =>
            val content = doc.baseContent
            currentProject.getTextEditorsOfPath(path) match {
              case Nil => currentProject.findOrCreateFile(path).setContent(content.text)
              case editors => editors.foreach(_.document.modifyTo(content.text))
            }
          case _ => publishCreateFile(path, event.file.isDirectory, content)
        }
      }
    }
  }

  def handleFileMoved(event: EditorFileMovedEvent): Unit = {
    (currentProject.getRelativePath(event.oldPath), currentProject.getRelativePath(event.newParentPath)) match {
      case (Some(path), Some(newParentPath)) => if (event.file.isDirectory) {
        myClient.publishEvent(new MoveDirEvent(path, newParentPath))
      } else {
        myClient.publishEvent(new MoveFileEvent(path, newParentPath))
      }
      case _ =>
    }
  }

  def handleFileRenamed(event: EditorFileRenamedEvent): Unit = {
    val oldPath = event.file.parent.path + "/" + event.oldName
    currentProject.getRelativePath(oldPath) match {
      case Some(old) => if (event.file.isDirectory) {
        myClient.publishEvent(RenameDirEvent(old, event.file.name))
      } else {
        myClient.publishEvent(RenameFileEvent(old, event.file.name))
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
    myClient.publishEvent(deleteEvent)
  }

  private def publishCreateFile(relativePath: String, isDirectory: Boolean, content: Option[Content]) {
    val createdEvent = if (isDirectory) {
      CreateDirEvent(relativePath)
    } else {
      CreateFileEvent(relativePath, content.get)
    }
    myClient.publishEvent(createdEvent)
  }

}
