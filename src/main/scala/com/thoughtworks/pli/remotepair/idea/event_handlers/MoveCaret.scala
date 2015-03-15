package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.remotepair.idea.core.{GetProjectInfoData, GetTextEditorsOfPath, PairCaretComponent}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class MoveCaret(invokeLater: InvokeLater, getTextEditorsOfPath: GetTextEditorsOfPath, isCaretSharing: IsCaretSharing) {
  val pairCaretComponentKey = new Key[PairCaretComponent]("pair-caret-component")
  def apply(path: String, offset: Int) {
    def caretPosition(editor: EditorEx, offset: Int) = {
      editor.logicalPositionToXY(editor.offsetToLogicalPosition(offset))
    }
    def createPairCaretInEditor(editor: EditorEx, offset: Int) = {
      var component = editor.getUserData[PairCaretComponent](pairCaretComponentKey)
      if (component == null) {
        component = new PairCaretComponent
        editor.getContentComponent.add(component)
        editor.putUserData(pairCaretComponentKey, component)
      }

      val viewport = editor.getContentComponent.getVisibleRect
      component.setBounds(0, 0, viewport.width, viewport.height)
      val position = caretPosition(editor, offset)
      if (position.x > 0) {
        position.x -= 1
      }

      component.setLocation(position)
      component.lineHeight = editor.getLineHeight
      component
    }

    def scrollToCaret(editor: EditorEx, offset: Int): Unit = {
      val position = caretPosition(editor, offset)
      if (!editor.getContentComponent.getVisibleRect.contains(position)) {
        editor.getScrollingModel.scrollTo(editor.xyToLogicalPosition(position), ScrollType.RELATIVE)
      }
    }

    getTextEditorsOfPath(path).foreach { editor =>
      invokeLater {
        try {
          val ex = editor.asInstanceOf[EditorEx]
          if (isCaretSharing()) {
            scrollToCaret(ex, offset)
          }
          createPairCaretInEditor(ex, offset).repaint()
        } catch {
          case e: Throwable => // FIXME fix it later
          // log.error("Error occurs when moving caret from pair: " + e.toString, e)
        }
      }
    }
  }

}

case class IsCaretSharing(getProjectInfoData: GetProjectInfoData) {
  def apply(): Boolean = getProjectInfoData().exists(_.isCaretSharing)
}
