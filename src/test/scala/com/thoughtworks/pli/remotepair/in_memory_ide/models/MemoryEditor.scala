package com.thoughtworks.pli.remotepair.in_memory_ide.models

import com.thoughtworks.pli.remotepair.core.models.{DataKey, HighlightTextAttrs, MyEditor}

class MemoryEditor(override val document: MemoryDocument, val filePath: String) extends MyEditor {
  private var _caret = 0

  private var userData = Map.empty[DataKey[_], Any]
  private var highlightSelections = Seq[Range]()
  private var _drawnCaret: Option[Int] = None
  private var _scrollTo: Option[Int] = None

  override def getUserData[T](key: DataKey[T]): Option[T] = userData.get(key).map(_.asInstanceOf[T])
  override def scrollToCaretInEditor(offset: Int): Unit = {
    _scrollTo = Some(offset)
  }
  override def putUserData[T](key: DataKey[T], value: T): Unit = {
    userData = userData + (key -> value)
  }
  override val caret: Int = _caret
  override def clearSelectionHighlight(): Seq[Range] = {
    val old = highlightSelections
    highlightSelections = Nil
    old
  }
  override def highlightSelection(attributes: HighlightTextAttrs, ranges: Seq[Range]): Unit = {
    highlightSelections = highlightSelections ++ ranges
  }
  override def drawCaretInEditor(offset: Int): Unit = {
    _drawnCaret = Some(offset)
  }

}
