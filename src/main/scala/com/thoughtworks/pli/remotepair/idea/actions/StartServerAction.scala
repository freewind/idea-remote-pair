package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.remotepair.core.DefaultValues
import com.thoughtworks.pli.remotepair.idea.Module

class StartServerAction extends AnAction("Start local server") {

  def actionPerformed(event: AnActionEvent) {
    new Module {
      override def currentIdeaRawProject: Project = event.getProject
    }.myServer.start(DefaultValues.DefaultServerPort)
  }

}



