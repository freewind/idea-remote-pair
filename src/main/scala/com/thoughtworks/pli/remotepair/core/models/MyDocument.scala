package com.thoughtworks.pli.remotepair.core.models

import com.intellij.openapi.editor.Document
import com.thoughtworks.pli.intellij.remotepair.utils.{Delete, Insert, StringDiff}

class MyDocument(document: Document)  {
  def length: Int = document.getTextLength
  def content_=(text: String): Unit = document.setText(text)
  def content: String = document.getText
  def insertString(offset: Int, newString: String): Unit = document.insertString(offset, newString)
  def deleteString(offset: Int, length: Int): Unit = document.deleteString(offset, length)
  def modifyTo(newContent: String): Unit = {
    val diffs = StringDiff.diffs(content, newContent)
    diffs.foreach {
      case Insert(offset, newStr) => document.insertString(offset, newStr)
      case Delete(offset, length) => document.deleteString(offset, offset + length)
    }
  }
}
