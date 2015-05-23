package com.thoughtworks.pli.remotepair.idea.editor

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.Key

class DrawCaretInEditor(convertEditorOffsetToPoint: ConvertEditorOffsetToPoint) {

  private val pairCaretComponentKey = new Key[PairCaretComponent]("pair-caret-component")

  def apply(editor: EditorEx, offset: Int): Unit = {
    var component = editor.getUserData[PairCaretComponent](pairCaretComponentKey)
    if (component == null) {
      component = new PairCaretComponent
      editor.getContentComponent.add(component)
      editor.putUserData(pairCaretComponentKey, component)
    }

    val viewport = editor.getContentComponent.getVisibleRect
    component.setBounds(0, 0, viewport.width, viewport.height)
    val position = convertEditorOffsetToPoint(editor, offset)
    if (position.x > 0) {
      position.x -= 1
    }

    component.setLocation(position)
    component.lineHeight = editor.getLineHeight
    component.repaint()
  }

}
