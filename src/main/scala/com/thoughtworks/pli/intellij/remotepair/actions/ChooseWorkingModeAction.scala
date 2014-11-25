package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.{Projects, InvokeLater}
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.WorkingModeDialog

class ChooseWorkingModeAction extends AnAction with InvokeLater {

  def actionPerformed(event: AnActionEvent) {
    val dialog = createDialog(event.getProject)
    dialog.show()
  }

  def createDialog(project: Project) = new WorkingModeDialog(Projects.init(project))

}
