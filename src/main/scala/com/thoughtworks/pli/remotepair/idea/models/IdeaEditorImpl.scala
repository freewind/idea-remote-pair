package com.thoughtworks.pli.remotepair.idea.models

import java.awt.{Color, Graphics, Point}
import javax.swing.JComponent

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.{HighlighterLayer, HighlighterTargetArea, RangeHighlighter, TextAttributes}
import com.intellij.openapi.editor.{Editor, ScrollType}
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.remotepair.core.models.{HighlightTextAttrs, MyDocument, MyEditor}

class IdeaEditorImpl(val rawEditor: Editor)(ideaFactories: IdeaFactories)
  extends MyEditor {
  require(rawEditor != null, "rawEditor should not be null")

  override def newHighlights(key: String, attributes: HighlightTextAttrs, ranges: Seq[Range]): Unit = {
    val hlAttrs = new TextAttributes(attributes.foregroundColor.orNull, attributes.backgroundColor.orNull, null, null, 0)
    val newHLs = ranges.map(r => {
      rawEditor.getMarkupModel.addRangeHighlighter(r.start, r.end, HighlighterLayer.LAST + 1, hlAttrs, HighlighterTargetArea.EXACT_RANGE)
    })
    rawEditor.putUserData(IdeaKeys.get(key), newHLs)
  }

  override def scrollToCaretInEditor(offset: Int): Unit = {
    val editor = rawEditor.asInstanceOf[EditorEx]
    val position = convertEditorOffsetToPoint(editor, offset)
    if (!editor.getContentComponent.getVisibleRect.contains(position)) {
      editor.getScrollingModel.scrollTo(editor.xyToLogicalPosition(position), ScrollType.RELATIVE)
    }
  }

  private def convertEditorOffsetToPoint(editor: EditorEx, offset: Int): Point = {
    editor.logicalPositionToXY(editor.offsetToLogicalPosition(offset))
  }
  override def removeOldHighlighters(key: String): Seq[Range] = {
    val oldHLs = Option(rawEditor.getUserData[Seq[RangeHighlighter]](IdeaKeys.get(key))).getOrElse(Nil)
    val oldRanges = oldHLs.map(hl => Range(hl.getStartOffset, hl.getEndOffset))
    oldHLs.foreach(rawEditor.getMarkupModel.removeHighlighter)
    oldRanges
  }
  override def document: MyDocument = ideaFactories(rawEditor.getDocument)

  import IdeaEditorImpl._

  override def drawCaretInEditor(offset: Int): Unit = {
    val editor = rawEditor.asInstanceOf[EditorEx]
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
  override def caret: Int = rawEditor.getCaretModel.getOffset
  override def getUserData[T](key: String): Option[T] = Option(rawEditor.getUserData(IdeaKeys.get(key)))
  override def putUserData[T](key: String, value: T): Unit = rawEditor.putUserData(IdeaKeys.get(key), value)

}

// FIXME
object IdeaEditorImpl {

  val pairCaretComponentKey = new Key[PairCaretComponent]("pair-caret-component")
  class PairCaretComponent extends JComponent {
    var lineHeight: Int = 0

    override def paint(g: Graphics): Unit = {
      g.setColor(Color.RED)
      g.fillRect(0, 0, 2, lineHeight)
      super.paint(g)
    }
  }

}
