package com.thoughtworks.pli.intellij.remotepair.actions

import org.specs2.mutable.Specification
import com.intellij.openapi.actionSystem.AnActionEvent
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.{ConnectServerDialogWrapper, ConnectServerDialogProvider}
import com.intellij.openapi.project.Project
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder

class ConnectServerActionSpec extends Specification with Mockito {

  "Connecting Dialog" should {
    "show when run action" in new Mocking {
      action.actionPerformed(event)
      there was one(dialog).show()
    }
  }

  trait Mocking extends Scope {
    val project = mock[Project]
    val dialog = mock[ConnectServerDialogWrapper]
    val action = new ConnectServerAction with ConnectServerDialogProvider with CurrentProjectHolder {
      override def currentProject = project
      override def createConnectServerDialog() = dialog
    }
    val event = mock[AnActionEvent]
  }

}
