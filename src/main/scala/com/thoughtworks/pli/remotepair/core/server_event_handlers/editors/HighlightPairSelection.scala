package com.thoughtworks.pli.remotepair.core.server_event_handlers.editors

import java.awt.Color

import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{HighlightTextAttrs, MyEditor, MyIde, MyProject}

class HighlightPairSelection(currentProject: MyProject, myIde: MyIde, myClient: MyClient, logger: PluginLogger) {

  def apply(event: SelectContentEvent) {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      myIde.invokeLater {
        removeOld(editor)
        highlightNew(editor, event.offset, event.offset + event.length, greenBackground)
      }
    }
  }

  private def greenBackground: HighlightTextAttrs = {
    new HighlightTextAttrs(backgroundColor = Some(Color.GREEN))
  }

  private def highlightNew(editor: MyEditor, start: Int, end: Int, attrs: HighlightTextAttrs) {
    try {
      if (start != end) {
        editor.highlightSelection(attrs, Seq(Range(start, end)))
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
      editor.clearSelectionHighlight()
    } catch {
      case e: Throwable => logger.error("Error occurs when removing old pair selection: " + e.toString, e)
    }
  }
}
