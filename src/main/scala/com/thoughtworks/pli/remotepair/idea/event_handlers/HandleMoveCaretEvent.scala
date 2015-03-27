package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.editor.ex.EditorEx
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

class HandleMoveCaretEvent(invokeLater: InvokeLater, getTextEditorsOfPath: GetTextEditorsOfPath, isCaretSharing: IsCaretSharing, scrollToCaretInEditor: ScrollToCaretInEditor, convertEditorOffsetToPoint: ConvertEditorOffsetToPoint, drawCaretInEditor: DrawCaretInEditor) {

  def apply(event: MoveCaretEvent): Unit = {
    getTextEditorsOfPath(event.path).foreach { editor =>
      invokeLater {
        try {
          val ex = editor.asInstanceOf[EditorEx]
          drawCaretInEditor(ex, event.offset)
          if (isCaretSharing()) {
            scrollToCaretInEditor(ex, event.offset)
          }
        } catch {
          case e: Throwable =>
        }
      }
    }
  }

}
