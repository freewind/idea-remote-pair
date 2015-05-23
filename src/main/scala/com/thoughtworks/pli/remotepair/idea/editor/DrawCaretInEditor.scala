package com.thoughtworks.pli.remotepair.idea.editor

import java.awt.{Color, Graphics, Point}
import javax.swing.JComponent

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.Key

class DrawCaretInEditor {

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

  private def convertEditorOffsetToPoint(editor: EditorEx, offset: Int): Point = {
    editor.logicalPositionToXY(editor.offsetToLogicalPosition(offset))
  }

  class PairCaretComponent extends JComponent {
    var lineHeight: Int = 0

    override def paint(g: Graphics): Unit = {
      g.setColor(Color.RED)
      g.fillRect(0, 0, 2, lineHeight)
      super.paint(g)
    }
  }

}
