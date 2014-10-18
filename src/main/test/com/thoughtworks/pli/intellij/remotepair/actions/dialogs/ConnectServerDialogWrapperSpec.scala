package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import org.specs2._
import org.specs2.specification.Scope
import com.intellij.openapi.project.Project
import org.specs2.mock.Mockito

class ConnectServerDialogWrapperSpec extends Specification with Mockito {
  override def is = s2"""

# ConnectServerDialogWrapper

ConnectServerDialogWrapper is used for inputting some necessary information to connect to a pair server.

It extends from `DialogWrapper`, which is provided by IDEA. It can wrap a normal swing dialog, giving
it some extra convenient behaviors, like "OK", "Close" buttons, closing when pressed "ESC" key, etc.

## Center dialog

We need to give it a custom dialog as its center dialog, $e1

## Values storing and retrieving

In the center dialog, the values of input text fields will be stored when user clicked the "connect" button.

- "target server host" will be stored on project level. $e3
- "target server port" will be stored on project level. $e4
- "client name" will be stored on application level. $e5

When the dialog is opened, they will be retrieved and set automatically. $e2

## Validation

The values of input text fields will be validated. If any of them is invalid (say, "port" is not a number),
the "connect" button is disabled, so user can't click on it.

In any one of the following situation, the "connect" button is disabled:

- server host is empty. $e6
- server port is empty. $e7
- server port is not integer. $e8
- server port is <= 0. $e9 $e10
- client name is empty. $e11

And if all fields are valid, the button is enabled. $e12

## Connecting

When user clicks on the "Connect" button,

- connect server with server host and port from form. $todo
- show error dialog if login failed. $todo

"""

  def e1 = new Mocking {
    new MockConnectServerDialogWrapper {}
    there was one(mockForm).getMainPanel
  }

  def e2 = new Mocking {
    new MockConnectServerDialogWrapper {}
    there was one(mockForm).init("aaa", 123, "bbb")
  }

  def e3 = new Mocking {
    val wrapper = new MockConnectServerDialogWrapper {
      override def connectToServer() {}
    }
    wrapper.doOKAction()
    there was one(wrapper.projectProperties).targetServerHost_=("aaa")
  }

  def e4 = new Mocking {
    val wrapper = new MockConnectServerDialogWrapper {
      override def connectToServer() {}
    }
    wrapper.doOKAction()
    there was one(wrapper.projectProperties).targetServerPort_=(123)
  }

  def e5 = new Mocking {
    val wrapper = new MockConnectServerDialogWrapper {
      override def connectToServer() {}
    }
    wrapper.doOKAction()
    there was one(wrapper.appProperties).clientName_=("bbb")
  }

  def e6 = new Mocking {
    val wrapper = new MockConnectServerDialogWrapper {}
    wrapper.form.getServerHostField.setText("")
    wrapper.isOKActionEnabled === false
  }

  def e7 = new Mocking {
    val wrapper = new MockConnectServerDialogWrapper {}
    wrapper.form.getServerPortField.setText("")
    wrapper.isOKActionEnabled === false
  }

  def e8 = new Mocking {
    val wrapper = new MockConnectServerDialogWrapper {}
    wrapper.form.getServerPortField.setText("1.1")
    wrapper.isOKActionEnabled === false
  }
  def e9 = new Mocking {
    val wrapper = new MockConnectServerDialogWrapper {}
    wrapper.form.getServerPortField.setText("0")
    wrapper.isOKActionEnabled === false
  }
  def e10 = new Mocking {
    val wrapper = new MockConnectServerDialogWrapper {}
    wrapper.form.getServerPortField.setText("-1")
    wrapper.isOKActionEnabled === false
  }
  def e11 = new Mocking {
    val wrapper = new MockConnectServerDialogWrapper {}
    wrapper.form.getClientNameField.setText("")
    wrapper.isOKActionEnabled === false
  }
  def e12 = new Mocking {
    val wrapper = new MockConnectServerDialogWrapper {}
    wrapper.isOKActionEnabled === true
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
