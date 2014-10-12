package com.thoughtworks.pli.intellij.remotepair

import net.liftweb.json.Serialization
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

trait ClientContextHolder {
  var context: Option[ChannelHandlerContext] = None
  var workerGroup: Option[NioEventLoopGroup] = None
}


trait CurrentProjectHolder {
  def currentProject: Project
}

trait Subscriber extends AppLogger with PublishEvents {
  this: ClientContextHolder with CurrentProjectHolder with EventHandler with ConnectionReadyEventsHolders =>

  class MyChannelHandler extends ChannelHandlerAdapter {

    override def channelActive(ctx: ChannelHandlerContext) {
      context = Some(ctx)
      grabAllReadyEvents().foreach(publishEvent)
    }

    override def channelInactive(ctx: ChannelHandlerContext) {
      context = None
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

  workerGroup = Some(new NioEventLoopGroup())

  val bootstrap = new Bootstrap()
  bootstrap.group(workerGroup.get)
  bootstrap.channel(classOf[NioSocketChannel])
  bootstrap.option(ChannelOption.SO_KEEPALIVE.asInstanceOf[ChannelOption[Any]], true)
  bootstrap.handler(MyChannelInitializer)

  def subscribe(ip: String, port: Int) {
    bootstrap.connect(ip, port)
  }

}

trait EventHandler extends OpenTabEventHandler with ModifyContentEventHandler with ResetContentEventHandler with Md5Support with EventParser {
  this: CurrentProjectHolder with PublishEvents with AppLogger =>

  def handleEvent(line: String) {
    println("Idea plugin receives line: " + line)
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

}


trait ModifyContentEventHandler extends InvokeLater {
  this: CurrentProjectHolder with AppLogger =>

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
      def run {
        f
      }
    })
  }

  def runReadAction(f: => Any) {
    ApplicationManager.getApplication.runReadAction(new Runnable {
      def run {
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

