package com.thoughtworks.pli.remotepair.idea.utils

import com.intellij.openapi.application.ApplicationManager

class InvokeLater {
  def apply(f: => Any): Unit = {
    ApplicationManager.getApplication.invokeLater(new Runnable {
      override def run(): Unit = f
    })
  }
}
