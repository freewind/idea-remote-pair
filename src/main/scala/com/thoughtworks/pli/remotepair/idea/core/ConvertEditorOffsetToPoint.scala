package com.thoughtworks.pli.remotepair.idea.core

import java.awt.Point

import com.intellij.openapi.editor.ex.EditorEx

class ConvertEditorOffsetToPoint {

  def apply(editor: EditorEx, offset: Int): Point = {
    editor.logicalPositionToXY(editor.offsetToLogicalPosition(offset))
  }

}
