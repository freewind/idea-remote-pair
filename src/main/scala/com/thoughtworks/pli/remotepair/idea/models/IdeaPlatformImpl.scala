package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.thoughtworks.pli.remotepair.core.models.MyPlatform

class IdeaPlatformImpl(currentProject: IdeaProjectImpl) extends MyPlatform {

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
  override def showErrorDialog(title: String, message: String): Unit = {
    Messages.showMessageDialog(currentProject.rawProject, message, title, Messages.getErrorIcon)
  }
}