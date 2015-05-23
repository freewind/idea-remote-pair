package com.thoughtworks.pli.remotepair.core.models

import com.intellij.openapi.editor.markup.TextAttributes

trait MyEditor {
  def newHighlights(key: String, attributes: TextAttributes, ranges: Seq[Range]): Unit
  def removeOldHighlighters(key: String): Seq[Range]
  def drawCaretInEditor(offset: Int)
  def scrollToCaretInEditor(offset: Int): Unit
  def document: MyDocument
  def caret: Int
  def getUserData[T](key: String): Option[T]
  def putUserData[T](key: String, value: T): Unit
}
