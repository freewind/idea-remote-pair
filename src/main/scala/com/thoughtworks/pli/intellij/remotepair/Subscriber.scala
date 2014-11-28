package com.thoughtworks.pli.intellij.remotepair

import java.nio.charset.Charset

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.{JoinProjectDialog, SendClientNameDialog, WorkingModeDialog}
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.ui.PairCaretComponent
import com.thoughtworks.pli.intellij.remotepair.utils.Md5Support
import io.netty.bootstrap.Bootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.{StringDecoder, StringEncoder}

trait Subscriber extends AppLogger with PublishEvents with EventHandler with EventParser {
  this: CurrentProjectHolder =>

  class MyChannelHandler extends ChannelHandlerAdapter {

    override def channelActive(ctx: ChannelHandlerContext) {
      currentProject.context = Some(ctx)
    }

    override def channelInactive(ctx: ChannelHandlerContext) {
      currentProject.context = None
      workerGroup.shutdownGracefully()
    }

    override def channelRead(ctx: ChannelHandlerContext, msg: Any) {
      msg match {
        case line: String =>
          println(s"Plugin ${currentProject.clientInfo.map(_.name).getOrElse("Unknown")} receives line: $line")
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

  var workerGroup: NioEventLoopGroup = _
  var bootstrap: Bootstrap = _

  def subscribe(ip: String, port: Int) = {
    workerGroup = new NioEventLoopGroup()
    bootstrap = new Bootstrap()
    bootstrap.group(workerGroup)
    bootstrap.channel(classOf[NioSocketChannel])
    bootstrap.option(ChannelOption.SO_KEEPALIVE.asInstanceOf[ChannelOption[Any]], true)
    bootstrap.handler(MyChannelInitializer)
    bootstrap.connect(ip, port).sync()
  }

}

trait EventHandler extends OpenTabEventHandler with ChangeContentEventHandler with ResetContentEventHandler with Md5Support with AppLogger with PublishEvents with DialogsCreator with CurrentProjectHolder {

  def handleEvent(event: PairEvent) {
    event match {
      case event: OpenTabEvent => handleOpenTabEvent(event.path)
      case event: CloseTabEvent =>
      case event: ChangeContentEvent => handleModifyContentEvent(event)
      case event: ResetContentEvent => handleResetContentEvent(event)
      case event: ResetTabEvent => handleOpenTabEvent(event.path)
      case event: ResetContentRequest => handleResetContentRequest(event)
      case ResetTabRequest => handleResetTabRequest()
      case event: MoveCaretEvent => moveCaret(event.path, event.offset)
      case event: SelectContentEvent => selectContent(event.path, event.offset, event.length)
      case event: ResetSelectionEvent => selectContent(event.path, event.offset, event.length)
      case event: ServerErrorResponse => showErrorDialog(event)
      case event: ServerStatusResponse => handleServerStatusResponse(event)
      case AskForClientInformation => handleAskForClientInformation()
      case AskForJoinProject => handleAskForJoinProject()
      case event: ClientInfoResponse => handleClientInfoResponse(event)
      case _ => println("!!!! Can't handle: " + event)
    }
  }

  private def handleClientInfoResponse(event: ClientInfoResponse) {
    currentProject.clientInfo = Some(event)
  }

  private def handleAskForClientInformation() {
    invokeLater(createSendClientNameDialog().show())
  }

  private def handleAskForJoinProject() {
    invokeLater(createJoinProjectDialog().show())
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

  private def handleResetTabRequest() {
    // FIXME it can be no opened tab
    invokeLater {
      val path = currentProject.pathOfSelectedTextEditor.getOrElse("")
      publishEvent(ResetTabEvent(path))
    }
  }

  val key = new Key[PairCaretComponent]("pair-caret-component")
  private def moveCaret(path: String, offset: Int) {
    def caretPosition(editor: EditorEx, offset: Int) = {
      editor.logicalPositionToXY(editor.offsetToLogicalPosition(offset))
    }
    def createPairCaretInEditor(editor: EditorEx, offset: Int) = {
      var component = editor.getUserData[PairCaretComponent](key)
      if (component == null) {
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> add new pairComponent!!!!!!!!!!!!!!!!!!! ")
        component = new PairCaretComponent
        editor.getContentComponent.add(component)
        editor.putUserData(key, component)
      }

      val viewport = editor.getContentComponent.getVisibleRect
      component.setBounds(0, 0, viewport.width, viewport.height)
      val position = caretPosition(editor, offset)
      if (position.x > 0) {
        position.x -= 1
      }

      component.setLocation(position)
      component.lineHeight = editor.getLineHeight
      component
    }

    def scrollToCaret(editor: EditorEx, offset: Int): Unit = {
      val position = caretPosition(editor, offset)
      if (!editor.getContentComponent.getVisibleRect.contains(position)) {
        editor.getScrollingModel.scrollTo(editor.xyToLogicalPosition(position), ScrollType.RELATIVE)
      }
    }

    currentProject.pairCarets.set(path, offset)
    currentProject.getTextEditorsOfPath(path).map(_.getEditor).foreach { editor =>
      invokeLater {
        val ex = editor.asInstanceOf[EditorEx]
        if (currentProject.projectInfo.exists(_.isCaretSharing)) {
          scrollToCaret(ex, offset)
        }
        createPairCaretInEditor(ex, offset).repaint()
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
    currentProject.serverStatus = Some(res)
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
