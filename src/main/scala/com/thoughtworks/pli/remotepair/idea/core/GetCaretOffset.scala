package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretEvent

class GetCaretOffset {
  def apply(event: CaretEvent) = event.getCaret.getOffset
  def apply(editor: Editor) = editor.getCaretModel.getOffset
}
