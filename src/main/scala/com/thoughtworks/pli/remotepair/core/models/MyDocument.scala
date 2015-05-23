package com.thoughtworks.pli.remotepair.core.models

trait MyDocument {

  def setContent(text: String): Unit
  def content: String
  def length: Int
  def insertString(offset: Int, newString: String)
  def deleteString(offset: Int, length: Int)
  def modifyTo(newContent: String): Unit

}
