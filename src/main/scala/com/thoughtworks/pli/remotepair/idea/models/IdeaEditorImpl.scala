package com.thoughtworks.pli.remotepair.idea.models

import java.awt.{Color, Graphics, Point}
import javax.swing.JComponent

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.{HighlighterLayer, HighlighterTargetArea, RangeHighlighter, TextAttributes}
import com.intellij.openapi.editor.{Editor, ScrollType}
import com.thoughtworks.pli.remotepair.core.models.{DataKey, HighlightTextAttrs, MyDocument, MyEditor}

class IdeaEditorImpl(val rawEditor: Editor)(ideaFactories: IdeaFactories)
  extends MyEditor {
  require(rawEditor != null, "rawEditor should not be null")

  private val highlightKey = new DataKey[Seq[RangeHighlighter]]("highlight.key")

  override def getUserData[T](key: DataKey[T]): Option[T] = Option(rawEditor.getUserData(IdeaKeys.get(key)))
  override def putUserData[T](key: DataKey[T], value: T): Unit = rawEditor.putUserData(IdeaKeys.get(key), value)
  override def highlightSelection(attributes: HighlightTextAttrs, ranges: Seq[Range]): Unit = {
    val hlAttrs = new TextAttributes(attributes.foregroundColor.orNull, attributes.backgroundColor.orNull, null, null, 0)
    val newHLs = ranges.map(r => {
      rawEditor.getMarkupModel.addRangeHighlighter(r.start, r.end, HighlighterLayer.LAST + 1, hlAttrs, HighlighterTargetArea.EXACT_RANGE)
    })
    rawEditor.putUserData(IdeaKeys.get(highlightKey), newHLs)
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
  override def clearSelectionHighlight(): Seq[Range] = {
    val oldHLs = Option(rawEditor.getUserData(IdeaKeys.get(highlightKey))).getOrElse(Nil)
    val oldRanges = oldHLs.map(hl => Range(hl.getStartOffset, hl.getEndOffset))
    oldHLs.foreach(rawEditor.getMarkupModel.removeHighlighter)
    oldRanges
  }
  override def document: MyDocument = ideaFactories(rawEditor.getDocument)

  override def drawCaretInEditor(offset: Int): Unit = {
    val pairCaretComponentKey = new DataKey[PairCaretComponent]("pair-caret-component")
    val editorEx = rawEditor.asInstanceOf[EditorEx]
    val component = getUserData(pairCaretComponentKey) match {
      case Some(c) => c
      case _ =>
        val c = new PairCaretComponent
        editorEx.getContentComponent.add(c)
        putUserData(pairCaretComponentKey, c)
        c
    }

    val viewport = editorEx.getContentComponent.getVisibleRect
    component.setBounds(0, 0, viewport.width, viewport.height)
    val position = convertEditorOffsetToPoint(editorEx, offset)
    if (position.x > 0) {
      position.x -= 1
    }

    component.setLocation(position)
    component.lineHeight = editorEx.getLineHeight
    component.repaint()
  }
  override def caret: Int = rawEditor.getCaretModel.getOffset

  class PairCaretComponent extends JComponent {
    var lineHeight: Int = 0

    override def paint(g: Graphics): Unit = {
      g.setColor(Color.RED)
      g.fillRect(0, 0, 2, lineHeight)
      super.paint(g)
    }
  }

}

