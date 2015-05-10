package com.thoughtworks.pli.remotepair.idea.utils

import com.intellij.openapi.command.WriteCommandAction
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

case class RunWriteAction(currentProject: IdeaProjectImpl) {
  def apply(f: => Any) {
    WriteCommandAction.runWriteCommandAction(currentProject.raw, new Runnable {
      override def run() {
        f
      }
    })
  }
}
