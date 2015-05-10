package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.{RangeHighlighter, TextAttributes}
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.remotepair.core.models.{MyDocument, MyEditor}
import com.thoughtworks.pli.remotepair.idea.editor.{DrawCaretInEditor, NewHighlights, RemoveOldHighlighters, ScrollToCaretInEditor}

class IdeaEditorImpl(val rawEditor: Editor)
                    (newHighlights: NewHighlights, scrollToCaretInEditor: ScrollToCaretInEditor, removeOldHighlighters: RemoveOldHighlighters, drawCaretInEditor: DrawCaretInEditor,
                     ideaFactories: IdeaFactories)
  extends MyEditor {
  require(rawEditor != null, "rawEditor should not be null")

  override def newHighlights(key: Key[Seq[RangeHighlighter]], attributes: TextAttributes, ranges: Seq[Range]): Unit = newHighlights(key, rawEditor, attributes, ranges)
  override def scrollToCaretInEditor(offset: Int): Unit = scrollToCaretInEditor(rawEditor.asInstanceOf[EditorEx], offset)
  override def removeOldHighlighters(key: Key[Seq[RangeHighlighter]]): Unit = removeOldHighlighters(key, rawEditor)
  override def document: MyDocument = ideaFactories(rawEditor.getDocument)
  override def drawCaretInEditor(offset: Int): Unit = drawCaretInEditor(rawEditor.asInstanceOf[EditorEx], offset)
  override def caret: Int = rawEditor.getCaretModel.getOffset
  override def getUserData[T](key: Key[T]): Option[T] = Option(rawEditor.getUserData(key))
  override def putUserData[T](key: Key[T], value: T): Unit = rawEditor.putUserData(key, value)
}
