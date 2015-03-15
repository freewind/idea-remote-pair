package com.thoughtworks.pli.remotepair.idea.utils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project

case class RunWriteAction(currentProject: Project) {
  def apply(f: => Any) {
    WriteCommandAction.runWriteCommandAction(currentProject, new Runnable {
      override def run() {
        f
      }
    })
  }
}
