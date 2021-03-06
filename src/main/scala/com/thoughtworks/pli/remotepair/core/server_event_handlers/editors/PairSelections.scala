package com.thoughtworks.pli.remotepair.core.server_event_handlers.editors

import java.awt.Color

import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyEditor, MyIde, MyProject}

class PairSelections(currentProject: MyProject, myIde: MyIde, myClient: MyClient, logger: PluginLogger) {

  def highlight(event: SelectContentEvent): Unit = {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      myIde.invokeLater {
        removeOldSelection(editor)
        highlightNewSelection(editor, event.offset, event.offset + event.length, greenBackground)
      }
    }
  }

  def clearAll(): Unit = currentProject.getAllOpenedTextEditors.foreach(removeOldSelection)

  private def greenBackground: HighlightTextAttrs = {
    new HighlightTextAttrs(backgroundColor = Some(Color.GREEN))
  }

  private def highlightNewSelection(editor: MyEditor, start: Int, end: Int, attrs: HighlightTextAttrs): Unit = {
    try {
      if (start != end) {
        editor.highlightSelection(attrs, Seq(Range(start, end)))
      }
    } catch {
      case e: Throwable => {
        logger.error("Error occurs when highlighting pair selection: " + e.toString, e)

        val len = editor.document.length
        highlightNewSelection(editor, Math.min(start, len), Math.min(end, len), attrs)
      }
    }
  }

  private def removeOldSelection(editor: MyEditor): Unit = {
    try {
      editor.clearSelectionHighlight()
    } catch {
      case e: Throwable => logger.error("Error occurs when removing old pair selection: " + e.toString, e)
    }
  }
}

case class HighlightTextAttrs(foregroundColor: Option[Color] = None, backgroundColor: Option[Color] = None)
