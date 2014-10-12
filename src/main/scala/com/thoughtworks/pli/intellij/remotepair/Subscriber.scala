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
        case line: String =>
          handleEvent(line)
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

trait EventHandler extends OpenTabEventHandler with ModifyContentEventHandler with ResetContentEventHandler with Md5Support {
  this: CurrentProjectHolder with PublishEvents with AppLogger =>

  def handleEvent(line: String) {
    val (name, json) = line.span(_ != ' ')
    println(s"######## name: [$name], json: [$json]")
    name match {
      case "OpenTabEvent" =>
        val event = Serialization.read[OpenTabEvent](json)
        handleOpenTabEvent(event.path)
      case "CloseTabEvent" =>
        val closeTabEvent = Serialization.read[CloseTabEvent](json)
        log.info("### CloseTabEvent: " + closeTabEvent)
      case "ChangeContentEvent" =>
        val event = Serialization.read[ChangeContentEvent](json)
        println("######### ModifyContentEvent: " + event)
        handleModifyContentEvent(json)
      case "ResetContentEvent" =>
        val event = Serialization.read[ResetContentEvent](json)
        handleResetContentEvent(event)
      case "ResetTabEvent" =>
        val event = Serialization.read[ResetTabEvent](json)
        handleOpenTabEvent(event.path)
      case "ResetContentRequest" =>
        val event = Serialization.read[ResetContentRequest](json)
        val fff = currentProject.getBaseDir.findFileByRelativePath(event.path)
        FileEditorManager.getInstance(currentProject).getAllEditors(fff).foreach { case editor: TextEditor =>
          runReadAction {
            val content = editor.getEditor.getDocument.getText
            val eee = new ResetContentEvent(event.path, content, md5(content))
            publishEvent(eee)
          }
        }
      case "ResetTabRequest" =>
        val event = Serialization.read[ResetTabRequest](json)
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
        invokeLater(publishEvent(event))
      case _ => println("############# Can't handle: " + line)
    }
  }


}


trait ModifyContentEventHandler extends InvokeLater {
  this: CurrentProjectHolder with AppLogger =>

  def handleModifyContentEvent(json: String) {
    println("######### json: " + json)
    val event = Serialization.read[ChangeContentEvent](json)
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

