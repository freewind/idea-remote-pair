package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.actions.forms.ConnectServerForm
import com.thoughtworks.pli.intellij.remotepair.client.MockInvokeLater
import com.thoughtworks.pli.intellij.remotepair.{MySpecification, RemotePairProjectComponent}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener
import org.mockito.Mockito.RETURNS_MOCKS
import org.mockito.{Mockito => JMockito}

class ConnectServerDialogSpec extends MySpecification {
  self =>

  isolated

  val project = mock[Project]
  val form = spy(new ConnectServerForm)
  val showError = mock[String => Any]

  val invokeLater = new MockInvokeLater

  class MockConnectServerDialog extends ConnectServerDialog(project) {

    object RunBeforeInitializing {
      val mockProjectProperties = mock[ProjectProperties]
      mockProperties(mockProjectProperties)
    }

    def mockProperties(mockProjectProperties: ProjectProperties) {
      mockProjectProperties.targetServerHost returns "aaa"
      mockProjectProperties.targetServerPort returns 123
    }

    override def form = self.form
    override def projectProperties = RunBeforeInitializing.mockProjectProperties
    override def invokeLater(f: => Any): Unit = self.invokeLater(f)
    override def showError(message: String) = self.showError(message)
  }

  def mockLoginStatus(successfully: Boolean) = {
    val channelFuture = mock[ChannelFuture]
    projectComponent.connect(any, any) returns channelFuture
    channelFuture.addListener(any[GenericFutureListener[ChannelFuture]]) answers { (param: Any) =>
      param match {
        case listener: GenericFutureListener[ChannelFuture]@unchecked =>
          val future = mock[ChannelFuture]
          future.isSuccess returns successfully
          listener.operationComplete(future)
        case _ => ???
      }
      channelFuture
    }
  }

  val projectComponent = mock[RemotePairProjectComponent](JMockito.withSettings.defaultAnswer(RETURNS_MOCKS))
  project.getComponent(classOf[RemotePairProjectComponent]) returns projectComponent

  lazy val dialog = new MockConnectServerDialog

  "When user clicked 'connect' button, it" should {
    "store 'target server host' value to project store" in {
      invokeLater(dialog.doOKAction()).await()
      there was one(dialog.projectProperties).targetServerHost_=("aaa")
    }
    "store 'target server port' value to project store" in {
      invokeLater(dialog.doOKAction()).await()
      there was one(dialog.projectProperties).targetServerPort_=(123)
    }
  }

  "When dialog is opened, it" should {
    "use the values from project store" in {
      invokeLater(dialog).await()
      there was one(form).host_=("aaa")
      there was one(form).port_=("123")
    }
    "enable 'Connect' button by default" in {
      var enabled = false
      invokeLater(enabled = dialog.isOKActionEnabled).await()
      enabled === true
    }
  }

  "When user click on the 'Connect' button, it" should {
    "validate user inputs" in {
      invokeLater(dialog.doValidate()).await()
      there was one(form).validate
    }
    "connect server with input host and port" in {
      invokeLater(dialog.connectToServer()).await()
      there was one(projectComponent).connect(form.host, form.port.toInt)
    }

    "show error if login failed" in {
      mockLoginStatus(successfully = false)
      invokeLater(dialog.connectToServer()).await()
      there was one(showError).apply("Can't connect to server aaa:123")
    }
    "close the dialog" in {
      mockLoginStatus(successfully = true)
      invokeLater(dialog.doOKAction()).await()

      // since `wrapper.close` is final, we can't mock or spy it
      // instead, I can only check the exit code which will be changed when I close the dialog
      dialog.getExitCode === 0
    }
  }

}

