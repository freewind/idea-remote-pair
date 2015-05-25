package com.thoughtworks.pli.remotepair.in_memory_ide.models

import com.thoughtworks.pli.remotepair.core.models.MyDocument

class MemoryDocument extends MyDocument {
  override var content: String = ""
  override def length: Int = content.length
  override def deleteString(offset: Int, length: Int): Unit = ???
  override def modifyTo(newContent: String): Unit = ???
  override def insertString(offset: Int, newString: String): Unit = ???

}
