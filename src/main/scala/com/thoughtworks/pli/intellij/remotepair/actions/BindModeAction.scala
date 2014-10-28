package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{CommonDataKeys, AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.thoughtworks.pli.intellij.remotepair.{CaretSharingModeRequest, RemotePairProjectComponent, InvokeLater}

class BindModeAction extends AnAction with InvokeLater {

  def actionPerformed(event: AnActionEvent) {
    val project = event.getData(CommonDataKeys.PROJECT)
    val targetUserName = askForTargetUserName(project)
    show(project, targetUserName)
  }

  private def askForTargetUserName(project: Project) = {
    Messages.showInputDialog(project, "Who do you want to bind", "Bind another user", Messages.getQuestionIcon)
  }

  private def show(project: Project, targetUserName: String) {
    val component = project.getComponent(classOf[RemotePairProjectComponent])

    invokeLater {
      component.publishEvent(CaretSharingModeRequest(targetUserName))
    }

  }
}
