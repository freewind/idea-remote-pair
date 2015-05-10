package com.thoughtworks.pli.remotepair.core.models

import com.intellij.openapi.editor.markup.{RangeHighlighter, TextAttributes}
import com.intellij.openapi.util.Key

trait MyEditor {
  def newHighlights(key: Key[Seq[RangeHighlighter]], attributes: TextAttributes, ranges: Seq[Range]): Unit
  def removeOldHighlighters(key: Key[Seq[RangeHighlighter]]): Unit
  def drawCaretInEditor(offset: Int)
  def scrollToCaretInEditor(offset: Int): Unit
  def document: MyDocument
  def caret: Int
  def getUserData[T](key: Key[T]): Option[T]
  def putUserData[T](key: Key[T], value: T): Unit
}
