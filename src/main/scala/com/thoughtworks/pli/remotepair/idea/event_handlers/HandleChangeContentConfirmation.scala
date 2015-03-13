package com.thoughtworks.pli.remotepair.idea.event_handlers

import java.awt.Color

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.markup.{RangeHighlighter, TextAttributes}
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.protocol.{ChangeContentConfirmation, Content}
import com.thoughtworks.pli.intellij.remotepair.utils.{Insert, StringDiff}
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.RunWriteAction

case class HandleChangeContentConfirmation(currentProject: RichProject, publishEvents: PublishEvent, runWriteAction: RunWriteAction, newHighlights: NewHighlights, removeOldHighlighters: RemoveOldHighlighters, logger: Logger, versionedDocuments: ClientVersionedDocuments) {

  private val changeContentHighlighterKey = new Key[Seq[RangeHighlighter]]("pair-change-content-highlighter")

  def apply(event: ChangeContentConfirmation): Unit = currentProject.getFileByRelative(event.path).foreach { file =>
    val doc = versionedDocuments.get(event.path)
    runWriteAction {
      try {
        doc.synchronized {
          val currentContent = currentProject.smartGetFileContent(file).text
          doc.handleContentChange(event, currentContent).map { targetContent =>
            currentProject.smartSetContentTo(event.path, Content(targetContent, file.getCharset.name()))
            highlightPairChanges(event.path, targetContent)
          }
        }
      } catch {
        case e: Throwable => logger.error("Error occurs when handling ChangeContentConfirmation: " + e.toString, e)
      }
    }
  }

  private def highlightPairChanges(path: String, targetContent: String) {
    val attrs = new TextAttributes(Color.GREEN, Color.YELLOW, null, null, 0)
    for {
      editor <- currentProject.getTextEditorsOfPath(path)
      currentContent = editor.getDocument.getCharsSequence.toString
      oldRanges = removeOldHighlighters(changeContentHighlighterKey, editor)
      diffs = StringDiff.diffs(currentContent, targetContent)
      newRanges = diffs.collect {
        case Insert(offset, content) => Range(offset, offset + content.length)
      }
      mergedRanges = mergeRanges(oldRanges, newRanges)
    } newHighlights(changeContentHighlighterKey, editor, attrs, mergedRanges)
  }

  private def mergeRanges(oldRanges: Seq[Range], newRanges: Seq[Range]): Seq[Range] = {
    // FIXME merge them
    newRanges
  }

}
