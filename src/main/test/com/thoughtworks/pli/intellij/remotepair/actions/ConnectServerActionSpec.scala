package com.thoughtworks.pli.intellij.remotepair.actions

import org.specs2.mutable.Specification
import com.intellij.openapi.actionSystem.AnActionEvent
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.ConnectServerDialogProvider
import com.thoughtworks.pli.intellij.remotepair.CurrentProjectHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

class ConnectServerActionSpec extends Specification with Mockito {

  "Connecting Dialog" should {
    "show when run action" in new Mocking {
      action.actionPerformed(event)
      there was one(dialog).show()
    }
  }

  trait Mocking extends Scope {
    val project = mock[Project]
    val dialog = mock[DialogWrapper]
    val action = new ConnectServerAction with ConnectServerDialogProvider with CurrentProjectHolder {
      override def currentProject = project
      override def createConnectServerDialog() = dialog
    }
    val event = mock[AnActionEvent]
  }

}
