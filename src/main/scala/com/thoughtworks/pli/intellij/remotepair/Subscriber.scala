package com.thoughtworks.pli.intellij.remotepair

import java.nio.charset.Charset

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.{JoinProjectDialog, SendClientNameDialog, WorkingModeDialog}
import com.thoughtworks.pli.intellij.remotepair.client.{ClientContextHolder, ClientInfoHolder, CurrentProjectHolder, ServerStatusHolder}
import com.thoughtworks.pli.intellij.remotepair.utils.Md5Support
import io.netty.bootstrap.Bootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.{StringDecoder, StringEncoder}

trait Subscriber extends AppLogger with PublishEvents with EventHandler with ServerStatusHolder with ClientContextHolder with EventParser {
  this: CurrentProjectHolder =>

  class MyChannelHandler extends ChannelHandlerAdapter {

    override def channelActive(ctx: ChannelHandlerContext) {
      context = Some(ctx)
    }

    override def channelInactive(ctx: ChannelHandlerContext) {
      context = None
      workerGroup.foreach(_.shutdownGracefully())
    }

    override def channelRead(ctx: ChannelHandlerContext, msg: Any) {
      msg match {
        case line: String =>
          println(s"Plugin ${clientInfo.map(_.name).getOrElse("Unknown")} receives line: $line")
          handleEvent(parseEvent(line))
        case _ =>
      }
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      cause.printStackTrace()
    }
  }

  object MyChannelInitializer extends ChannelInitializer[SocketChannel] {
    override def initChannel(ch: SocketChannel) {
      ch.pipeline().addLast(
        new LineBasedFrameDecoder(Int.MaxValue),
        new StringDecoder(Charset.forName("UTF-8")),
        new StringEncoder(Charset.forName("UTF-8")),
        new MyChannelHandler())
    }
  }

  private val workerGroup = Some(new NioEventLoopGroup())

  val bootstrap = new Bootstrap()
  bootstrap.group(workerGroup.get)
  bootstrap.channel(classOf[NioSocketChannel])
  bootstrap.option(ChannelOption.SO_KEEPALIVE.asInstanceOf[ChannelOption[Any]], true)
  bootstrap.handler(MyChannelInitializer)

  def subscribe(ip: String, port: Int) = {
    bootstrap.connect(ip, port)
  }

}

trait EventHandler extends OpenTabEventHandler with ChangeContentEventHandler with ResetContentEventHandler with Md5Support with AppLogger with PublishEvents with DialogsCreator with ServerStatusHolder with ClientContextHolder with ClientInfoHolder with CurrentProjectHolder {

  def handleEvent(event: PairEvent) {
    event match {
      case event: OpenTabEvent => handleOpenTabEvent(event.path)
      case event: CloseTabEvent =>
      case event: ChangeContentEvent => handleModifyContentEvent(event)
      case event: ResetContentEvent => handleResetContentEvent(event)
      case event: ResetTabEvent => handleOpenTabEvent(event.path)
      case event: ResetContentRequest => handleResetContentRequest(event)
      case event: ResetTabRequest => handleResetTabRequest(event)
      case event: MoveCaretEvent => moveCaret(event.path, event.offset)
      case event: ResetCaretEvent => moveCaret(event.path, event.offset)
      case event: SelectContentEvent => selectContent(event.path, event.offset, event.length)
      case event: ResetSelectionEvent => selectContent(event.path, event.offset, event.length)
      case event: ServerErrorResponse => showErrorDialog(event)
      case event: ServerStatusResponse => handleServerStatusResponse(event)
      case AskForClientInformation => handleAskForClientInformation()
      case AskForJoinProject => handleAskForJoinProject()
      case AskForWorkingMode => handleAskForWorkingMode()
      case event: ClientInfoResponse => handleClientInfoResponse(event)
      case _ => println("!!!! Can't handle: " + event)
    }
  }

  private def handleClientInfoResponse(event: ClientInfoResponse) {
    clientInfo = Some(event)
  }

  private def handleAskForClientInformation() {
    invokeLater(createSendClientNameDialog().show())
  }

  private def handleAskForJoinProject() {
    invokeLater(createJoinProjectDialog().show())
  }

  private def handleAskForWorkingMode() {
    invokeLater(createWorkingModeDialog().show())
  }

  private def handleResetContentRequest(event: ResetContentRequest) {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      runReadAction {
        val content = editor.getEditor.getDocument.getText
        val eee = new ResetContentEvent(event.path, content, md5(content))
        publishEvent(eee)
      }
    }
  }

  private def handleResetTabRequest(event: ResetTabRequest) {
    val path = currentProject.pathOfSelectedTextEditor.getOrElse("")
    // FIXME it can be no opened tab
    invokeLater(publishEvent(ResetTabEvent(path)))
  }

  private def moveCaret(path: String, offset: Int) {
    currentProject.getTextEditorsOfPath(path).foreach { editor =>
      invokeLater {
        editor.getEditor.getCaretModel.moveToOffset(offset)
      }
    }
  }

  private def selectContent(path: String, offset: Int, length: Int) {
    currentProject.getTextEditorsOfPath(path).foreach { editor =>
      invokeLater {
        editor.getEditor.getSelectionModel.setSelection(offset, offset + length)
      }
    }
  }

  private def showErrorDialog(res: ServerErrorResponse) {
    currentProject.showErrorDialog("Get error message from server", res.message)
  }

  private def handleServerStatusResponse(res: ServerStatusResponse) {
    serverStatus = res
  }

}

trait ChangeContentEventHandler extends InvokeLater with AppLogger with PublishEvents {
  this: CurrentProjectHolder =>

  def handleModifyContentEvent(event: ChangeContentEvent) {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      runWriteAction {
        try {
          editor.getEditor.getDocument.replaceString(event.offset, event.offset + event.oldFragment.length, event.newFragment)
        } catch {
          case e: Throwable => publishEvent(ResetContentRequest(event.path))
        }
      }
    }
  }

}

trait OpenTabEventHandler extends InvokeLater with AppLogger {
  this: CurrentProjectHolder =>

  def handleOpenTabEvent(path: String) = {
    openTab(path)(currentProject)
  }

  private def openTab(path: String)(project: RichProject) {
    currentProject.getByRelative(path).foreach { file =>
      val openFileDescriptor = project.openFileDescriptor(file)
      println("#### openFileDescriptor.canNavigate: " + openFileDescriptor.canNavigate)
      if (openFileDescriptor.canNavigate) {
        invokeLater {
          println("########## navigate start!!!!")
          openFileDescriptor.navigate(true)
          println("########## navigate finished!!!!")
        }
      }
    }
  }

}

trait InvokeLater {
  def invokeLater(f: => Any) {
    ApplicationManager.getApplication.invokeLater(new Runnable {
      override def run(): Unit = f
    })
  }

  def runWriteAction(f: => Any) {
    WriteCommandAction.runWriteCommandAction(null, new Runnable {
      override def run() {
        f
      }
    })
  }

  def runReadAction(f: => Any) {
    ApplicationManager.getApplication.runReadAction(new Runnable {
      override def run() {
        f
      }
    })
  }
}

trait ResetContentEventHandler extends InvokeLater with AppLogger {
  this: CurrentProjectHolder =>

  def handleResetContentEvent(event: ResetContentEvent) = {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      runWriteAction {
        editor.getEditor.getDocument.setText(event.content)
      }
    }
  }

}

trait DialogsCreator {
  this: CurrentProjectHolder =>
  def createSendClientNameDialog() = new SendClientNameDialog(currentProject)
  def createJoinProjectDialog() = new JoinProjectDialog(currentProject)
  def createWorkingModeDialog() = new WorkingModeDialog(currentProject)
}
