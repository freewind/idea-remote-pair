package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ex.EditorEx

class ScrollToCaretInEditor(convertEditorOffsetToPoint: ConvertEditorOffsetToPoint) {

  def apply(editor: EditorEx, offset: Int): Unit = {
    val position = convertEditorOffsetToPoint(editor, offset)
    if (!editor.getContentComponent.getVisibleRect.contains(position)) {
      editor.getScrollingModel.scrollTo(editor.xyToLogicalPosition(position), ScrollType.RELATIVE)
    }
  }

}
