package com.thoughtworks.pli.remotepair.idea.utils

import com.intellij.openapi.command.WriteCommandAction
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

case class RunWriteAction(currentProject: RichProject) {
  def apply(f: => Any) {
    WriteCommandAction.runWriteCommandAction(currentProject.raw, new Runnable {
      override def run() {
        f
      }
    })
  }
}
