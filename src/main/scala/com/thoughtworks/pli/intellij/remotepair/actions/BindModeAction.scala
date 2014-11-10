package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{CommonDataKeys, AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.{CaretSharingModeRequest, RemotePairProjectComponent, InvokeLater}

class BindModeAction extends AnAction with InvokeLater {

  def actionPerformed(event: AnActionEvent) {
    val project = event.getData(CommonDataKeys.PROJECT)
    show(project)
  }

  private def show(project: Project) {
    val component = project.getComponent(classOf[RemotePairProjectComponent])

    invokeLater {
      component.publishEvent(CaretSharingModeRequest)
    }

  }
}
