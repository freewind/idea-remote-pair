package com.thoughtworks.pli.remotepair.core.server_event_handlers.editors

import java.awt.Color

import com.intellij.openapi.editor.markup.{RangeHighlighter, TextAttributes}
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.PublishEvent
import com.thoughtworks.pli.remotepair.core.models.{MyEditor, MyPlatform, MyProject}

class HighlightPairSelection(currentProject: MyProject, myPlatform: MyPlatform, publishEvent: PublishEvent, logger: PluginLogger) {

  private val key = HighlightPairSelection.SelectionHighlightKey

  def apply(event: SelectContentEvent) {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      myPlatform.invokeLater {
        removeOld(editor)
        highlightNew(editor, event.offset, event.offset + event.length, greenBackground)
      }
    }
  }

  private def greenBackground: TextAttributes = {
    new TextAttributes(null, Color.GREEN, null, null, 0)
  }

  private def highlightNew(editor: MyEditor, start: Int, end: Int, attrs: TextAttributes) {
    try {
      if (start != end) {
        editor.newHighlights(key, attrs, Seq(Range(start, end)))
      }
    } catch {
      case e: Throwable => {
        logger.error("Error occurs when highlighting pair selection: " + e.toString, e)

        val len = editor.document.length
        highlightNew(editor, Math.min(start, len), Math.min(end, len), attrs)
      }
    }
  }

  private def removeOld(editor: MyEditor) {
    try {
      editor.removeOldHighlighters(key)
    } catch {
      case e: Throwable => logger.error("Error occurs when removing old pair selection: " + e.toString, e)
    }
  }
}

object HighlightPairSelection {
  val SelectionHighlightKey = new Key[Seq[RangeHighlighter]]("pair-selection-highlighter")
}
