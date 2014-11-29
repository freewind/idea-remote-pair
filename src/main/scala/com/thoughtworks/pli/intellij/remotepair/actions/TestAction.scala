package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.Projects
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.ChooseIgnoreDialog

class TestAction extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val dialog = createDialog(event.getProject)
    dialog.show()
  }

  private def createDialog(project: Project) = new ChooseIgnoreDialog(Projects.init(project))
}
