package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import com.intellij.openapi.project.Project
import org.specs2.mock.Mockito

class ConnectServerDialogWrapperSpec extends Specification with Mockito {

  "ConnectServerDialogWrapper" should {
    "set form with project stored values" in new Mocking {
      new MockConnectServerDialogWrapper {}
      there was one(mockForm).init("aaa", 123, "bbb")
    }
    "use main panel from ConnectServerForm as center panel" in new Mocking {
      new MockConnectServerDialogWrapper {}
      there was one(mockForm).getMainPanel
    }
  }

  "Click on 'OK' button" should {
    "store target server host on project level" in new Mocking {
      val wrapper = new MockConnectServerDialogWrapper {
        override def connectToServer() {}
      }
      wrapper.doOKAction()
      there was one(wrapper.projectProperties).targetServerHost_=("aaa")
    }
    "store target server port on project level" in new Mocking {
      val wrapper = new MockConnectServerDialogWrapper {
        override def connectToServer() {}
      }
      wrapper.doOKAction()
      there was one(wrapper.projectProperties).targetServerPort_=(123)
    }
    "store client on application level" in new Mocking {
      val wrapper = new MockConnectServerDialogWrapper {
        override def connectToServer() {}
      }
      wrapper.doOKAction()
      there was one(wrapper.appProperties).clientName_=("bbb")
    }
  }

  "Validation" should {
    "disable OK button if server host is empty" in new Mocking {
      val wrapper = new MockConnectServerDialogWrapper {}
      wrapper.form.getServerHostField.setText("")
      wrapper.isOKActionEnabled === false
    }
    "disable OK button if server port is empty" in new Mocking {
      val wrapper = new MockConnectServerDialogWrapper {}
      wrapper.form.getServerPortField.setText("")
      wrapper.isOKActionEnabled === false
    }
    "disable OK button if server port is not integer" in new Mocking {
      val wrapper = new MockConnectServerDialogWrapper {}
      wrapper.form.getServerPortField.setText("1.1")
      wrapper.isOKActionEnabled === false
    }
    "disable OK button if server port is == 0" in new Mocking {
      val wrapper = new MockConnectServerDialogWrapper {}
      wrapper.form.getServerPortField.setText("0")
      wrapper.isOKActionEnabled === false
    }
    "disable OK button if server port is < 0" in new Mocking {
      val wrapper = new MockConnectServerDialogWrapper {}
      wrapper.form.getServerPortField.setText("-1")
      wrapper.isOKActionEnabled === false
    }
    "disable OK button if client name is empty" in new Mocking {
      val wrapper = new MockConnectServerDialogWrapper {}
      wrapper.form.getClientNameField.setText("")
      wrapper.isOKActionEnabled === false
    }
    "enable OK button if all the fields are good" in new Mocking {
      val wrapper = new MockConnectServerDialogWrapper {}
      wrapper.isOKActionEnabled === true
    }
  }

  "Click on 'OK' button" should {
    "connect server with server host and port from form" in todo
    "show error dialog if login failed" in todo
  }

  trait Mocking extends Scope {

    val project = mock[Project]
    val mockForm = spy(new ConnectServerForm)

    abstract class MockConnectServerDialogWrapper extends ConnectServerDialogWrapper(project) {

      object RunBeforeInitializing {
        val mockAppProperties = mock[AppProperties]
        val mockProjectProperties = mock[ProjectProperties]
        mockProperties(mockAppProperties, mockProjectProperties)
      }

      def mockProperties(mockAppProperties: AppProperties, mockProjectProperties: ProjectProperties) {
        mockProjectProperties.targetServerHost returns "aaa"
        mockProjectProperties.targetServerPort returns 123
        mockAppProperties.clientName returns "bbb"
      }

      override def createForm() = mockForm
      override def projectProperties = RunBeforeInitializing.mockProjectProperties
      override def appProperties = RunBeforeInitializing.mockAppProperties
      override def invokeLater(f: => Any): Unit = f
    }

  }

}
