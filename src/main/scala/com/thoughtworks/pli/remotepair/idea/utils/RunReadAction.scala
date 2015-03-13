package com.thoughtworks.pli.remotepair.idea.utils

import com.intellij.openapi.application.ApplicationManager

class RunReadAction {
  def apply(f: => Any) {
    ApplicationManager.getApplication.runReadAction(new Runnable {
      override def run() {
        f
      }
    })
  }
}
