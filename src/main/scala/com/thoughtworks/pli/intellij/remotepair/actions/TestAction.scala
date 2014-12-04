package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.{Projects, RichProject}

class TestAction extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    new X(Projects.init(event.getProject)).go()
  }

  class X(override val currentProject: RichProject) extends CurrentProjectHolder {
    def go(): Unit = {
      val file = currentProject.getFileByRelative("/src/AAA.java").get

    }
  }

}
