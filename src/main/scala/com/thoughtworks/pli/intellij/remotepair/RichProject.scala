package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.{FileDocumentManager, FileEditorManager, OpenFileDescriptor, TextEditor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.{StatusBar, WindowManager}
import com.intellij.util.messages.Topic
import com.thoughtworks.pli.intellij.remotepair.server.Server
import io.netty.channel.ChannelHandlerContext

import scala.reflect.ClassTag

object Projects {
  var projects = Map.empty[Project, RichProject]
  def init(project: Project): RichProject = this.synchronized {
    projects.get(project) match {
      case Some(rich) => rich
      case _ => val rich = new RichProject(project)
        projects += project -> rich
        rich
    }
  }
  def remove(project: Project): Unit = this.synchronized {
    projects -= project
  }
}

trait PluginHelpers {

  val raw: Project

  def openFileDescriptor(file: VirtualFile) = new OpenFileDescriptor(raw, file)

  def fileEditorManager: FileEditorManager = {
    FileEditorManager.getInstance(raw)
  }
  def getSelectedTextEditor: Option[Editor] = Option(fileEditorManager.getSelectedTextEditor)
  def getRelativePath(file: VirtualFile): String = {
    file.getPath.replace(raw.getBasePath, "")
  }
  def pathOfSelectedTextEditor: Option[String] = getSelectedTextEditor
    .map(editor => FileDocumentManager.getInstance().getFile(editor.getDocument))
    .map(getRelativePath)

  def showMessageDialog(message: String) = {
    Messages.showMessageDialog(raw, message, "Information", Messages.getInformationIcon)
  }

  def showErrorDialog(title: String, message: String) = {
    Messages.showMessageDialog(raw, message, title, Messages.getErrorIcon)
  }

  def getStatusBar: StatusBar = {
    WindowManager.getInstance().getStatusBar(raw)
  }

  def getTextEditorsOfPath(path: String): Seq[TextEditor] = {
    getFile(path).map(file => fileEditorManager.getAllEditors(file).toSeq.collect { case e: TextEditor => e}).getOrElse(Nil)
  }

  def getComponent[T: ClassTag]: T = {
    val cls = implicitly[ClassTag[T]].runtimeClass
    raw.getComponent(cls).asInstanceOf[T]
  }

  def getByRelative(path: String): Option[VirtualFile] = Option(raw.getBaseDir.findFileByRelativePath(path))

  def getFile(path: String): Option[VirtualFile] = {
    Option(raw.getBaseDir.findFileByRelativePath(path))
  }

  def getMessageBus = raw.getMessageBus

  def createMessageConnection() = {
    getMessageBus.connect(raw)
  }
}

object Topics {
  val ProjectStatusTopic: Topic[ProjectStatusChangeListener] = Topic.create("Project status notifications", classOf[ProjectStatusChangeListener])
}

trait ProjectStatusChangeListener {
  def onChange(): Unit
}

case class RichProject(raw: Project) extends PluginHelpers {

  def getName = raw.getName

  var _context: Option[ChannelHandlerContext] = None
  def context: Option[ChannelHandlerContext] = _context
  def context_=(ctx: Option[ChannelHandlerContext]): Unit = {
    _context = ctx
    notifyBasicChanges()
  }

  var _serverStatus: Option[ServerStatusResponse] = None
  def serverStatus: Option[ServerStatusResponse] = _serverStatus
  def serverStatus_=(status: Option[ServerStatusResponse]): Unit = {
    _serverStatus = status
    notifyBasicChanges()
  }

  var _clientInfo: Option[ClientInfoResponse] = None
  def clientInfo: Option[ClientInfoResponse] = _clientInfo
  def clientInfo_=(info: Option[ClientInfoResponse]): Unit = {
    _clientInfo = info
    notifyBasicChanges()
  }

  var _server: Option[Server] = None
  def server: Option[Server] = _server
  def server_=(server: Option[Server]) = {
    _server = server
    notifyBasicChanges()
  }

  val pairCarets = new PairCarets

  def projectInfo: Option[ProjectInfoData] = for {
    server <- serverStatus
    client <- clientInfo
    projectName <- client.project
    p <- server.projects.find(_.name == projectName)
  } yield p

  private def notifyBasicChanges(): Unit = {
    val publisher = getMessageBus.syncPublisher(Topics.ProjectStatusTopic)
    publisher.onChange()
  }
}


class PairCarets {

  var maps = Map.empty[String, Int]

  def set(path: String, offset: Int) = {
    maps += path -> offset
  }

  def get(path: String): Option[Int] = maps.get(path)

}
