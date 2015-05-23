package com.thoughtworks.pli.remotepair.core.models

import java.awt.Color

trait MyEditor {
  def newHighlights(key: String, attributes: HighlightTextAttrs, ranges: Seq[Range]): Unit
  def removeOldHighlighters(key: String): Seq[Range]
  def drawCaretInEditor(offset: Int)
  def scrollToCaretInEditor(offset: Int): Unit
  def document: MyDocument
  def caret: Int
  def getUserData[T](key: String): Option[T]
  def putUserData[T](key: String, value: T): Unit
}

case class HighlightTextAttrs(foregroundColor: Option[Color] = None, backgroundColor: Option[Color] = None)
