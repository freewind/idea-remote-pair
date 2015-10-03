package com.thoughtworks.pli.remotepair.in_memory_ide.models

import com.thoughtworks.pli.remotepair.core.models.MyDocument

class MemoryDocument(var content: String) extends MyDocument {
  override def length: Int = content.length
  override def deleteString(offset: Int, length: Int): Unit = {
    content = content.substring(0, offset) + content.substring(offset + length)
  }
  override def modifyTo(newContent: String): Unit = {
    content = newContent
  }
  override def insertString(offset: Int, newString: String): Unit = {
    content = content.substring(0, offset) + newString + content.substring(offset)
  }
}
