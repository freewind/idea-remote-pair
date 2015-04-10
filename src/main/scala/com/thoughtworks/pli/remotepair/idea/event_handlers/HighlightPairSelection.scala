package com.thoughtworks.pli.remotepair.idea.event_handlers

import java.awt.Color

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.{RangeHighlighter, TextAttributes}
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

class HighlightPairSelection(getTextEditorsOfPath: GetTextEditorsOfPath, invokeLater: InvokeLater, publishEvent: PublishEvent, newHighlights: NewHighlights, removeOldHighlighters: RemoveOldHighlighters, logger: Logger, getDocumentLength: GetDocumentLength) {

  private val key = HighlightPairSelection.SelectionHighlightKey

  def apply(event: SelectContentEvent) {
    getTextEditorsOfPath(event.path).foreach { editor =>
      invokeLater {
        removeOld(editor)
        highlightNew(editor, event.offset, event.offset + event.length, greenBackground)
      }
    }
  }

  private def greenBackground: TextAttributes = {
    new TextAttributes(null, Color.GREEN, null, null, 0)
  }

  private def highlightNew(editor: Editor, start: Int, end: Int, attrs: TextAttributes) {
    try {
      if (start != end) {
        newHighlights(key, editor, attrs, Seq(Range(start, end)))
      }
    } catch {
      case e: Throwable => {
        logger.error("Error occurs when highlighting pair selection: " + e.toString, e)

        val len = getDocumentLength(editor)
        highlightNew(editor, Math.min(start, len), Math.min(end, len), attrs)
      }
    }
  }

  private def removeOld(editor: Editor) {
    try {
      removeOldHighlighters(key, editor)
    } catch {
      case e: Throwable => logger.error("Error occurs when removing old pair selection: " + e.toString, e)
    }
  }
}

object HighlightPairSelection {
  val SelectionHighlightKey = new Key[Seq[RangeHighlighter]]("pair-selection-highlighter")
}
