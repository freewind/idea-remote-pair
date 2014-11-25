package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.{FileDocumentManager, FileEditorManager, OpenFileDescriptor, TextEditor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.{StatusBar, WindowManager}
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

}

case class RichProject(raw: Project) extends PluginHelpers {

  def getName = raw.getName

  var context: Option[ChannelHandlerContext] = None

  var serverStatus: Option[ServerStatusResponse] = None

  var clientInfo: Option[ClientInfoResponse] = None

  def projectInfo: Option[ProjectInfoData] = for {
    server <- serverStatus
    client <- clientInfo
    projectName <- client.project
    p <- server.projects.find(_.name == projectName)
  } yield p

}
