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

class ConnectServerDialogWrapperSpec extends Specification with Mockito with ThrownExpectations {
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
- "client name" will be stored on application level. $e5

When the dialog is opened, they will be retrieved and set automatically. $e2

## Validation

The values of input text fields will be validated. If any of them is invalid (say, "port" is not a number),
the "connect" button is disabled, so user can't click on it.

In any one of the following situation, the "connect" button is disabled:

- "server host" is empty. $e6
- "server port" is empty. $e7
- "server port" is not integer. $e8
- "server port" is <= 0. $e9 $e10
- "client name" is empty. $e11

And if all fields are valid, the button is enabled. $e12

## Connecting

When user clicks on the "Connect" button,

- connect server with server host and port. $e13
- show error if login failed. $e14
- if login successfully, start the client initialization process. $e15
- close the dialog then. $e16

The "initialization process" mentioned above, will try to send all the necessary information to server,
e.g. client name, creating/joining project, choosing working mode, etc.

"""

  def e1 = new Mocking {
    there was one(mockForm).getMainPanel
  }

  def e2 = new Mocking {
    there was one(mockForm).init("aaa", 123, "bbb")
  }

  def e3 = new Mocking {
    wrapper.doOKAction()
    there was one(wrapper.projectProperties).targetServerHost_=("aaa")
  }

  def e4 = new Mocking {
    wrapper.doOKAction()
    there was one(wrapper.projectProperties).targetServerPort_=(123)
  }

  def e5 = new Mocking {
    wrapper.doOKAction()
    there was one(wrapper.appProperties).clientName_=("bbb")
  }

  def e6 = new Mocking {
    wrapper.form.getServerHostField.setText("")
    wrapper.isOKActionEnabled === false
  }

  def e7 = new Mocking {
    wrapper.form.getServerPortField.setText("")
    wrapper.isOKActionEnabled === false
  }

  def e8 = new Mocking {
    wrapper.form.getServerPortField.setText("1.1")
    wrapper.isOKActionEnabled === false
  }

  def e9 = new Mocking {
    wrapper.form.getServerPortField.setText("0")
    wrapper.isOKActionEnabled === false
  }

  def e10 = new Mocking {
    wrapper.form.getServerPortField.setText("-1")
    wrapper.isOKActionEnabled === false
  }

  def e11 = new Mocking {
    wrapper.form.getClientNameField.setText("")
    wrapper.isOKActionEnabled === false
  }

  def e12 = new Mocking {
    wrapper.isOKActionEnabled === true
  }

  def e13 = new Mocking {
    wrapper.connectToServer()
    there was one(projectComponent).connect(mockForm.getHost, mockForm.getPort.toInt)
  }

  def e14 = new Mocking {
    mockLoginStatus(successfully = false)

    wrapper.connectToServer()
    await()

    there was one(mockForm).setMessage("Can't connect to server aaa:123")
  }

  def e15 = new Mocking {
    mockLoginStatus(successfully = true)

    wrapper.connectToServer()
    await()

    there was one(initializingProcess).start()
  }

  def e16 = new Mocking {
    mockLoginStatus(successfully = true)

    wrapper.connectToServer()
    await()

    // since `wrapper.close` is final, we can't mock or spy it
    // instead, I can only check the exit code which will be changed when I close the dialog
    wrapper.getExitCode === 0
  }

  trait Mocking extends Scope {

    val project = mock[Project]
    val mockForm = spy(new ConnectServerForm)
    val promise: Promise[Unit] = Promise[Unit]()
    val initializingProcess = mock[InitializingProcess]

    class MockConnectServerDialogWrapper extends ConnectServerDialogWrapper(project) {

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
      override def createInitializingProcess() = initializingProcess
      override def projectProperties = RunBeforeInitializing.mockProjectProperties
      override def appProperties = RunBeforeInitializing.mockAppProperties
      override def invokeLater(f: => Any): Unit = java.awt.EventQueue.invokeLater(new Runnable {
        override def run(): Unit = try {
          f
          promise.success(())
        } catch {
          case e: Throwable => promise.failure(e)
        }
      })
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

    val wrapper = new MockConnectServerDialogWrapper
  }

}
