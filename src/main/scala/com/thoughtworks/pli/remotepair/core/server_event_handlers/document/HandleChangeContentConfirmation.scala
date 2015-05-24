package com.thoughtworks.pli.remotepair.core.server_event_handlers.document

import java.awt.Color

import com.thoughtworks.pli.intellij.remotepair.protocol.{ChangeContentConfirmation, Content, GetDocumentSnapshot}
import com.thoughtworks.pli.intellij.remotepair.utils.{Insert, StringDiff}
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{HighlightTextAttrs, MyFile, MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.ClientVersionedDocuments

import scala.util.{Failure, Success}

class HandleChangeContentConfirmation(currentProject: MyProject, myClient: MyClient, myIde: MyIde, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments) {

  def apply(event: ChangeContentConfirmation): Unit = {
    (currentProject.getFileByRelative(event.path), clientVersionedDocuments.find(event.path)) match {
      case (Some(file), Some(doc)) => myIde.runWriteAction {
        try {
          doc.synchronized {
            val Content(currentContent, _) = tryBestToGetFileContent(file)
            doc.handleContentChange(event, currentContent) match {
              case Success(Some(targetContent)) =>
                currentProject.getTextEditorsOfPath(event.path) match {
                  case Nil => currentProject.findOrCreateFile(event.path).setContent(targetContent)
                  case editors => editors.foreach(_.document.modifyTo(targetContent))
                }
                highlightNewContent(event.path, targetContent)
              case Failure(e) => requestSnapshot(event)
              case Success(None) =>
            }
          }
        } catch {
          case e: Throwable => logger.error("Error occurs when handling ChangeContentConfirmation: " + e.toString, e)
        }
      }
      case _ => requestSnapshot(event)
    }
  }

  def requestSnapshot(event: ChangeContentConfirmation): Unit = {
    myClient.myClientId.foreach(myId => myClient.publishEvent(GetDocumentSnapshot(myId, event.path)))
  }

  private def tryBestToGetFileContent(file: MyFile) = {
    file.cachedContent.getOrElse(file.content)
  }

  private def highlightNewContent(path: String, newContent: String) {
    val attrs = new HighlightTextAttrs(Some(Color.GREEN), Some(Color.YELLOW))
    for {
      editor <- currentProject.getTextEditorsOfPath(path)
      oldRanges = editor.clearSelectionHighlight()
      diffs = StringDiff.diffs(editor.document.content, newContent)
      newRanges = diffs.collect {
        case Insert(offset, content) => Range(offset, offset + content.length)
      }
    } editor.highlightSelection(attrs, newRanges)
  }

}
