package com.thoughtworks.pli.intellij.remotepair

import javax.swing.tree.DefaultMutableTreeNode

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.{FileDocumentManager, FileEditorManager, OpenFileDescriptor, TextEditor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.{StatusBar, WindowManager}
import com.intellij.util.messages.Topic
import com.thoughtworks.pli.intellij.remotepair.server.Server
import io.netty.channel.ChannelHandlerContext
import org.apache.commons.io.IOUtils

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
    getFileByRelative(path).map(file => fileEditorManager.getAllEditors(file).toSeq.collect { case e: TextEditor => e}).getOrElse(Nil)
  }

  def getComponent[T: ClassTag]: T = {
    val cls = implicitly[ClassTag[T]].runtimeClass
    raw.getComponent(cls).asInstanceOf[T]
  }

  def getFileByRelative(path: String): Option[VirtualFile] = Option(raw.getBaseDir.findFileByRelativePath(path))

  def getMessageBus = raw.getMessageBus

  def createMessageConnection() = {
    getMessageBus.connect(raw)
  }

  def getBaseDir: VirtualFile = raw.getBaseDir

  def getContentAsString(file: VirtualFile): String = {
    IOUtils.toString(file.getInputStream, file.getCharset.name())
  }

  def forceWriteTextFile(relativePath: String, content: String): Unit = {
    getTextEditorsOfPath(relativePath) match {
      case Nil => val file = getFileByRelative(relativePath).getOrElse(findOrCreateFile(relativePath))
        file.setBinaryContent(content.getBytes("UTF-8"))
      case editors => editors.foreach { editor =>
        editor.getEditor.getDocument.setText(content)
        getDocumentManager.saveDocument(editor.getEditor.getDocument)
      }
    }
  }

  def getDocumentManager: FileDocumentManager = FileDocumentManager.getInstance()


  def findOrCreateFile(relativePath: String): VirtualFile = {
    val pathItems = relativePath.split("/")
    findOrCreateDir(pathItems.init.mkString("/")).findOrCreateChildData(this, pathItems.last)
  }

  def findOrCreateDir(relativePath: String): VirtualFile = {
    relativePath.split("/").filter(_.length > 0).foldLeft(getBaseDir) {
      case (file, name) => {
        Option(file.findChild(name)).fold(file.createChildDirectory(this, name))(identity)
      }
    }

  }

  def containsFile(file: VirtualFile): Boolean = {
    file.getPath == raw.getBaseDir.getPath || file.getPath.startsWith(raw.getBaseDir.getPath + "/")
  }


}

object Topics {
  val ProjectStatusTopic: Topic[ProjectStatusChangeListener] = Topic.create("Project status notifications", classOf[ProjectStatusChangeListener])
}

trait ProjectStatusChangeListener {
  def onChange(): Unit
}

case class RichProject(raw: Project) extends PluginHelpers with ProjectFilesSupport {
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

trait ProjectFilesSupport {
  this: PluginHelpers =>

  case class MyTreeNodeData(file: VirtualFile) {
    override def toString: String = file.getName
  }

  def getAllPairableFiles(ignoredFiles: Seq[String]): Seq[VirtualFile] = {
    val tree = buildFileTree(getBaseDir, ignoredFiles)
    tree.asList.filterNot(_.isDirectory).filterNot(_.getFileType.isBinary)
  }

  private def buildFileTree(rootDir: VirtualFile, ignoredFiles: Seq[String]): FileTree = {
    def fetchChildFiles(node: DefaultMutableTreeNode): Unit = {
      val data = node.getUserObject.asInstanceOf[MyTreeNodeData]
      if (data.file.isDirectory) {
        data.file.getChildren.foreach { c =>
          if (!isIgnored(c, ignoredFiles)) {
            val child = new FileTreeNode(MyTreeNodeData(c))
            node.add(child)
            fetchChildFiles(child)
          }
        }
      }
    }
    val rootNode = new FileTreeNode(MyTreeNodeData(rootDir))
    fetchChildFiles(rootNode)
    FileTree(rootNode)
  }

  private def isIgnored(file: VirtualFile, ignoredFiles: Seq[String]): Boolean = {
    val relativePath = getRelativePath(file)
    ignoredFiles.exists(p => relativePath == p || relativePath.startsWith(p + "/"))
  }

  case class FileTreeNode(data: MyTreeNodeData) extends DefaultMutableTreeNode(data) {
    override def hashCode(): Int = {
      data.file.hashCode()
    }

    override def equals(o: scala.Any): Boolean = o match {
      case d: DefaultMutableTreeNode => d.getUserObject match {
        case dd: MyTreeNodeData => dd.file == data.file
        case _ => false
      }
      case _ => false
    }
  }

  case class FileTree(root: FileTreeNode) {
    def asList: List[VirtualFile] = {
      def fetchChildren(node: FileTreeNode, result: List[VirtualFile]): List[VirtualFile] = {
        if (node.getChildCount == 0) result
        else {
          val nodes = (0 until node.getChildCount).map(node.getChildAt).map(_.asInstanceOf[FileTreeNode]).toList
          nodes.foldLeft(nodes.map(_.data.file) ::: result) {
            case (all, child) => fetchChildren(child, all)
          }
        }
      }
      fetchChildren(root, Nil)
    }
  }

}

