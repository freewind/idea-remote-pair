package com.thoughtworks.pli.remotepair.in_memory_ide.models

import com.thoughtworks.pli.remotepair.core.models.{DataKey, MyDocument, HighlightTextAttrs, MyEditor}

class MemoryEditor extends MyEditor {
  override def getUserData[T](key: DataKey[T]): T = ???
  override def scrollToCaretInEditor(offset: Int): Unit = ???
  override def putUserData[T](key: DataKey[T], value: T): Unit = ???
  override def caret: Int = ???
  override def document: MyDocument = ???
  override def clearSelectionHighlight(): Seq[Range] = ???
  override def highlightSelection(attributes: HighlightTextAttrs, ranges: Seq[Range]): Unit = ???
  override def drawCaretInEditor(offset: Int): Unit = ???
}
