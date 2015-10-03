package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.thoughtworks.pli.remotepair.core.models.MyIde

class IdeaIdeImpl(currentProject: IdeaProjectImpl) extends MyIde {

  override def invokeLater(f: => Any): Unit = {
    ApplicationManager.getApplication.invokeLater(new Runnable {
      override def run(): Unit = f
    })
  }
  override def runWriteAction(f: => Any): Unit = {
    WriteCommandAction.runWriteCommandAction(currentProject.rawProject, new Runnable {
      override def run() {
        f
      }
    })
  }
  override def runReadAction(f: => Any): Unit = {
    ApplicationManager.getApplication.runReadAction(new Runnable {
      override def run() {
        f
      }
    })
  }
}
