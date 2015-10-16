package com.thoughtworks.pli.remotepair.core.models

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction

class MyIde(currentProject: MyProject) {

  def invokeLater(f: => Any): Unit = {
    ApplicationManager.getApplication.invokeLater(new Runnable {
      override def run(): Unit = f
    })
  }
  def runWriteAction(f: => Any): Unit = {
    WriteCommandAction.runWriteCommandAction(currentProject.rawProject, new Runnable {
      override def run() {
        f
      }
    })
  }
  def runReadAction(f: => Any): Unit = {
    ApplicationManager.getApplication.runReadAction(new Runnable {
      override def run() {
        f
      }
    })
  }
}
