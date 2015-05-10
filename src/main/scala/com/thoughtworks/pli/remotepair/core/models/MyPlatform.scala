package com.thoughtworks.pli.remotepair.core.models

trait MyPlatform {
  def showErrorDialog(title: String, message: String): Unit
  def invokeLater(f: => Any): Unit
  def runReadAction(f: => Any): Unit
  def runWriteAction(f: => Any): Unit
}
