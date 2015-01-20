package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.thoughtworks.pli.remotepair.idea.core._

class TestAction extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    new X(Projects.init(event.getProject)).go()
  }

  class X(override val currentProject: RichProject) extends CurrentProjectHolder with AppLogger with InvokeLater {
    def go(): Unit = {
      //      val file = currentProject.getFileByRelative("/src/AAA.java").get
      //
      runWriteAction {
        currentProject.getSelectedTextEditor.foreach { editor =>
          val doc = editor.getDocument
          doc.insertString(0, "aaa")
          log.info("Insert 'aaa'")
          doc.insertString(0, "bbb")
          log.info("Insert 'bbb'")
          doc.insertString(0, "ccc")
          log.info("Insert 'ccc'")
        }
      }
    }
  }

}
