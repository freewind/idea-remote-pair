package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener
import org.specs2._
import org.specs2.specification.Scope
import com.intellij.openapi.project.Project
import org.specs2.mock.Mockito
import com.thoughtworks.pli.intellij.remotepair.RemotePairProjectComponent
import org.specs2.matcher.ThrownExpectations
import org.mockito.Mockito.RETURNS_MOCKS
import org.mockito.{Mockito => JMockito}
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration.{MILLISECONDS, Duration}
import com.thoughtworks.pli.intellij.remotepair.client.InitializingProcess
import com.thoughtworks.pli.intellij.remotepair.actions.forms.{_ConnectServerForm, ConnectServerForm}

class ConnectServerDialogSpec extends Specification with Mockito with ThrownExpectations {
  override def is = s2"""

# ConnectServerDialogWrapper

ConnectServerDialogWrapper is used for inputting some necessary information to connect to a pair server.

It extends from `DialogWrapper`, which is provided by IDEA. It can wrap a normal swing dialog, giving
it some extra convenient behaviors, like "OK", "Close" buttons, closing when pressed "ESC" key, etc.

## Center dialog

We need to give it a custom dialog as its center dialog. $e1

## Values storing and retrieving

In the center dialog, the values of input text fields will be stored when user clicked the "connect" button.

- "target server host" will be stored on project level. $e3
- "target server port" will be stored on project level. $e4

When the dialog is opened, they will be retrieved and set automatically. $e2

## Validation

The "Connect" button is enabled at first. $e12

We can provide some validation for the fields, that when user click on "Connect" button, the validation will be executed.

If validation is failed (say, "port" is not a number), IDEA will show error message and focus on the invalid field
based on the validation result, and automatically disable the "Connect" button. $e17

## Connecting

When user clicks on the "Connect" button,

- connect server with server host and port. $e13
- show error if login failed. $e14
- if login successfully, start the client initialization process. $e15
- close the dialog then. $e16

The "initialization process" mentioned above, will try to send all the necessary information to server,
e.g. client name, creating/joining project, choosing working mode, etc.

"""

  private def e1 = new Mocking {
    there was one(form).mainPanel
  }

  private def e2 = new Mocking {
    there was one(form).host_=("aaa")
    there was one(form).port_=("123")
  }

  private def e3 = new Mocking {
    dialog.doOKAction()
    there was one(dialog.projectProperties).targetServerHost_=("aaa")
  }

  private def e4 = new Mocking {
    dialog.doOKAction()
    there was one(dialog.projectProperties).targetServerPort_=(123)
  }


  private def e12 = new Mocking {
    dialog.isOKActionEnabled === true
  }

  private def e17 = new Mocking {
    dialog.doValidate()

    there was one(form).validate
  }

  private def e13 = new Mocking {
    dialog.connectToServer()
    await()
    there was one(projectComponent).connect(form.host, form.port.toInt)
  }

  private def e14 = new Mocking {
    mockLoginStatus(successfully = false)

    dialog.connectToServer()
    await()

    errorMessage === "Can't connect to server aaa:123"
  }

  private def e15 = new Mocking {
    mockLoginStatus(successfully = true)

    dialog.connectToServer()
    await()

    there was one(initializingProcess).start()
  }

  private def e16 = new Mocking {
    mockLoginStatus(successfully = true)

    dialog.connectToServer()
    await()

    // since `wrapper.close` is final, we can't mock or spy it
    // instead, I can only check the exit code which will be changed when I close the dialog
    dialog.getExitCode === 0
  }

  trait Mocking extends Scope {
    self =>

    val project = mock[Project]
    val form = spy(new ConnectServerForm())
    val promise: Promise[Unit] = Promise[Unit]()
    val initializingProcess = mock[InitializingProcess]
    var errorMessage: String = _

    class MockConnectServerDialog extends ConnectServerDialog(project) {

      object RunBeforeInitializing {
        val mockProjectProperties = mock[ProjectProperties]
        mockProperties(mockProjectProperties)
      }

      def mockProperties(mockProjectProperties: ProjectProperties) {
        mockProjectProperties.targetServerHost returns "aaa"
        mockProjectProperties.targetServerPort returns 123
      }

      override def createForm() = self.form
      override def createInitializingProcess() = initializingProcess
      override def projectProperties = RunBeforeInitializing.mockProjectProperties
      override def invokeLater(f: => Any): Unit = java.awt.EventQueue.invokeLater(new Runnable {
        override def run(): Unit = try {
          f
          promise.success(())
        } catch {
          case e: Throwable => promise.failure(e)
        }
      })
      override def showError(message: String) {
        errorMessage = message
      }
    }

    def mockLoginStatus(successfully: Boolean) = {
      val channelFuture = mock[ChannelFuture]
      projectComponent.connect(any, any) returns channelFuture
      channelFuture.addListener(any[GenericFutureListener[ChannelFuture]]) answers { (param: Any) =>
        param match {
          case listener: GenericFutureListener[ChannelFuture] =>
            val future = mock[ChannelFuture]
            future.isSuccess returns successfully
            listener.operationComplete(future)
          case _ => ???
        }
        channelFuture
      }
    }

    def await() {
      Await.ready(promise.future, Duration(500, MILLISECONDS))
    }

    val projectComponent = mock[RemotePairProjectComponent](JMockito.withSettings.defaultAnswer(RETURNS_MOCKS))
    project.getComponent(classOf[RemotePairProjectComponent]) returns projectComponent

    val dialog = new MockConnectServerDialog
  }

}
