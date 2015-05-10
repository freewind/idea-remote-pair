package com.thoughtworks.pli.remotepair.core.models

trait MyDocument {

  def setContent(text: String): Unit
  def content: String
  def length: Int

}
