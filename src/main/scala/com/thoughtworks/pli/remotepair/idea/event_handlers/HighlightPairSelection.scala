package com.thoughtworks.pli.remotepair.idea.event_handlers

import java.awt.Color

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.markup.{RangeHighlighter, TextAttributes}
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.idea.core.{GetTextEditorsOfPath, NewHighlights, PublishEvent, RemoveOldHighlighters}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class HighlightPairSelection(getTextEditorsOfPath: GetTextEditorsOfPath, invokeLater: InvokeLater, publishEvent: PublishEvent, newHighlights: NewHighlights, removeOldHighlighters: RemoveOldHighlighters, logger: Logger) {
  private val key = new Key[Seq[RangeHighlighter]]("pair-selection-highlighter")

  def apply(event: SelectContentEvent) {
    getTextEditorsOfPath(event.path).foreach { editor =>
      invokeLater {
        try {
          removeOldHighlighters(key, editor)
          val (start, end) = (event.offset, event.offset + event.length)
          if (start != end) {
            val attrs = new TextAttributes(null, Color.GREEN, null, null, 0)
            newHighlights(key, editor, attrs, Seq(Range(start, end, 1)))
          }
        } catch {
          case e: Throwable => logger.error("Error occurs when highlighting pair selection: " + e.toString, e)
        }
      }
    }
  }

}
