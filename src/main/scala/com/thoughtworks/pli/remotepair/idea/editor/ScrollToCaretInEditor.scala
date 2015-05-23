package com.thoughtworks.pli.remotepair.idea.editor

import java.awt.Point

import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ex.EditorEx

class ScrollToCaretInEditor {

  def apply(editor: EditorEx, offset: Int): Unit = {
    val position = convertEditorOffsetToPoint(editor, offset)
    if (!editor.getContentComponent.getVisibleRect.contains(position)) {
      editor.getScrollingModel.scrollTo(editor.xyToLogicalPosition(position), ScrollType.RELATIVE)
    }
  }

  private def convertEditorOffsetToPoint(editor: EditorEx, offset: Int): Point = {
    editor.logicalPositionToXY(editor.offsetToLogicalPosition(offset))
  }

}
