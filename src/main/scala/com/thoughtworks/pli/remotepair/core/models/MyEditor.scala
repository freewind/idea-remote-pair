package com.thoughtworks.pli.remotepair.core.models

import java.awt.Color

trait MyEditor {
  def getUserData[T](key: DataKey[T]): Option[T]
  def putUserData[T](key: DataKey[T], value: T): Unit
  def highlightSelection(attributes: HighlightTextAttrs, ranges: Seq[Range]): Unit
  def clearSelectionHighlight(): Seq[Range]
  def drawCaretInEditor(offset: Int)
  def scrollToCaretInEditor(offset: Int): Unit
  def document: MyDocument
  def caret: Int
}

case class HighlightTextAttrs(foregroundColor: Option[Color] = None, backgroundColor: Option[Color] = None)
