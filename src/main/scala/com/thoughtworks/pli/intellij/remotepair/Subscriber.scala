package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.project.Project
import com.intellij.openapi.fileEditor.{FileDocumentManager, TextEditor, FileEditorManager, OpenFileDescriptor}
import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel._
import io.netty.channel.socket.SocketChannel
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.thoughtworks.pli.intellij.remotepair.utils.Md5Support
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.{StringEncoder, StringDecoder}
import java.nio.charset.Charset
import com.intellij.openapi.ui.Messages
import com.thoughtworks.pli.intellij.remotepair.client.{ServerStatusHolder, CurrentProjectHolder, ClientContextHolder}

trait Subscriber extends AppLogger with PublishEvents with EventHandler with ServerStatusHolder with ClientContextHolder {
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
        case line: String => handleEvent(line)
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

trait EventHandler extends OpenTabEventHandler with ModifyContentEventHandler with ResetContentEventHandler with Md5Support with EventParser with AppLogger with PublishEvents {
  this: CurrentProjectHolder with ServerStatusHolder with ClientContextHolder =>

  def handleEvent(line: String) {
    println(s"plugin receives line: $line")
    parseEvent(line) match {
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
      case _ => println("############# Can't handle: " + line)
    }
  }

  private def handleResetContentRequest(event: ResetContentRequest) {
    val fff = currentProject.getBaseDir.findFileByRelativePath(event.path)
    FileEditorManager.getInstance(currentProject).getAllEditors(fff).foreach { case editor: TextEditor =>
      runReadAction {
        val content = editor.getEditor.getDocument.getText
        val eee = new ResetContentEvent(event.path, content, md5(content))
        publishEvent(eee)
      }
    }
  }

  private def handleResetTabRequest(event: ResetTabRequest) {
    val ddd = FileEditorManager.getInstance(currentProject).getSelectedTextEditor
    val eee = if (ddd != null) {
      val f = FileDocumentManager.getInstance().getFile(ddd.getDocument)
      def mypath(f: String, project: Project) = {
        val sss = f.replace(project.getBasePath, "")
        println("######## path: " + sss)
        sss
      }
      new ResetTabEvent(mypath(f.getPath, currentProject))
    } else {
      new ResetTabEvent("")
    }
    invokeLater(publishEvent(eee))
  }

  private def moveCaret(path: String, offset: Int) {
    val fff = currentProject.getBaseDir.findFileByRelativePath(path)
    FileEditorManager.getInstance(currentProject).getAllEditors(fff).foreach { case editor: TextEditor =>
      invokeLater {
        editor.getEditor.getCaretModel.moveToOffset(offset)
      }
    }
  }

  private def selectContent(path: String, offset: Int, length: Int) {
    val fff = currentProject.getBaseDir.findFileByRelativePath(path)
    FileEditorManager.getInstance(currentProject).getAllEditors(fff).foreach { case editor: TextEditor =>
      invokeLater {
        editor.getEditor.getSelectionModel.setSelection(offset, offset + length)
      }
    }
  }

  private def showErrorDialog(res: ServerErrorResponse) {
    Messages.showMessageDialog(currentProject, res.message, "Get error message from server", Messages.getErrorIcon)
  }

  private def handleServerStatusResponse(res: ServerStatusResponse) {
    serverStatus = res
  }

}

trait ModifyContentEventHandler extends InvokeLater with AppLogger {
  this: CurrentProjectHolder =>

  def handleModifyContentEvent(event: ChangeContentEvent) {
    val fff = currentProject.getBaseDir.findFileByRelativePath(event.path)
    FileEditorManager.getInstance(currentProject).getAllEditors(fff).foreach { case editor: TextEditor =>
      runWriteAction {
        editor.getEditor.getDocument.replaceString(event.offset, event.offset + event.oldFragment.length, event.newFragment)
      }
    }
  }

}

trait OpenTabEventHandler extends InvokeLater {
  this: CurrentProjectHolder with AppLogger =>

  def handleOpenTabEvent(path: String) = {
    openTab(path)(currentProject)
  }

  private def openTab(path: String)(project: Project) {
    val virtualFile = project.getBaseDir.findFileByRelativePath(path)
    if (virtualFile == null) {
      return
    }

    val openFileDescriptor = new OpenFileDescriptor(project, virtualFile)
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

trait ResetContentEventHandler extends InvokeLater {
  this: CurrentProjectHolder with AppLogger =>

  def handleResetContentEvent(event: ResetContentEvent) = {
    val fff = currentProject.getBaseDir.findFileByRelativePath(event.path)
    FileEditorManager.getInstance(currentProject).getAllEditors(fff).foreach { case editor: TextEditor =>
      runWriteAction {
        editor.getEditor.getDocument.setText(event.content)
      }
    }
  }

}

