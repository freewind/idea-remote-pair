package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.WorkingModeDialog
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ChooseWorkingModeActionSpec extends Specification with Mockito {

  "ChooseWorkingModeAction" should {
    "show dialog when performed" in new Mocking {
      action.actionPerformed(event)
      there was one(dialog).show()
    }
  }

  trait Mocking extends Scope {
    val dialog = mock[WorkingModeDialog]
    val event = mock[AnActionEvent]
    val action = new ChooseWorkingModeAction {
      override def createDialog(project: Project) = dialog
    }
  }

}
