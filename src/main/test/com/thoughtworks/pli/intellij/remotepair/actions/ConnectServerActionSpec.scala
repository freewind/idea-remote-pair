package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.MySpecification
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.{ConnectServerDialog, MockCurrentProjectHolder}
import org.specs2.specification.Scope

class ConnectServerActionSpec extends MySpecification {

  "Connecting Dialog" should {
    "show when run action" in new Mocking {
      action.actionPerformed(event)
      there was one(dialog).show()
    }
  }

  trait Mocking extends Scope with MockCurrentProjectHolder {
    val dialog = mock[ConnectServerDialog]
    val action = new ConnectServerAction {
      override def createDialog(project: Project) = dialog
    }
    val event = mock[AnActionEvent]
  }

}
