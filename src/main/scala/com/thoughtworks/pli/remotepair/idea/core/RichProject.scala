package com.thoughtworks.pli.remotepair.idea.core

import javax.swing.tree.DefaultMutableTreeNode

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.{StatusBar, WindowManager}
import com.intellij.util.messages.{MessageBus, MessageBusConnection}
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.intellij.remotepair.utils._
import com.thoughtworks.pli.remotepair.idea.core.ClientVersionedDocumentFactory.ClientVersionedDocument
import com.thoughtworks.pli.remotepair.idea.core.ConnectionFactory.Connection
import com.thoughtworks.pli.remotepair.idea.core.MyChannelHandlerFactory.MyChannelHandler
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

import scala.reflect.ClassTag

object RichProjectFactory {
  type RichProject = RichProjectFactory#create
}

case class RichProjectFactory(md5: Md5, isSubPath: IsSubPath, runtimeAssertions: RuntimeAssertions) {

  case class create(raw: Project) extends ProjectContext with IdeaApiWrappers with IdeaEditorSupport with ProjectPathSupport with IdeaMessageDialogs with ProjectFilesSupport {
    def getName = raw.getName
    @volatile var channelHandler: Option[MyChannelHandler] = None
  }

  trait ProjectFilesSupport {
    this: create =>

    // FIXME now it's wathing, not ignore"
    def getAllWatchingiles(ignoredFiles: Seq[String]): Seq[VirtualFile] = {
      val tree = buildFileTree(getBaseDir, ignoredFiles)
      toList(tree).filterNot(_.isDirectory).filterNot(_.getFileType.isBinary)
    }

    def getPairableFileSummaries: Seq[FileSummary] = getAllWatchingiles(watchingFiles).flatMap(getFileSummary)

    private def buildFileTree(rootDir: VirtualFile, ignoredFiles: Seq[String]): FileTree = {
      def fetchChildFiles(node: DefaultMutableTreeNode): Unit = {
        val data = node.getUserObject.asInstanceOf[FileTreeNodeData]
        if (data.file.isDirectory) {
          data.file.getChildren.foreach { c =>
            if (!isIgnored(c, ignoredFiles)) {
              val child = new FileTreeNode(FileTreeNodeData(c))
              node.add(child)
              fetchChildFiles(child)
            }
          }
        }
      }
      val rootNode = new FileTreeNode(FileTreeNodeData(rootDir))
      fetchChildFiles(rootNode)
      FileTree(rootNode)
    }

    private def isIgnored(file: VirtualFile, ignoredFiles: Seq[String]): Boolean = {
      ignoredFiles.exists(base => getRelativePath(file).exists(p => isSubPath(p, base)))
    }

    private def toList(tree: FileTree): List[VirtualFile] = {
      def fetchChildren(node: FileTreeNode, result: List[VirtualFile]): List[VirtualFile] = {
        if (node.getChildCount == 0) result
        else {
          val nodes = (0 until node.getChildCount).map(node.getChildAt).map(_.asInstanceOf[FileTreeNode]).toList
          nodes.foldLeft(nodes.map(_.data.file) ::: result) {
            case (all, child) => fetchChildren(child, all)
          }
        }
      }
      fetchChildren(tree.root, Nil)
    }

  }


  trait IdeaMessageDialogs {
    this: create =>
    def showMessageDialog(message: String) = {
      Messages.showMessageDialog(raw, message, "Information", Messages.getInformationIcon)
    }

    def showErrorDialog(title: String = "Error", message: String) = {
      Messages.showMessageDialog(raw, message, title, Messages.getErrorIcon)
    }
  }


  trait ProjectPathSupport {
    this: create =>

    import runtimeAssertions.{goodPath, hasParentPath}

    def getBaseDir: VirtualFile = raw.getBaseDir
    def getBasePath: String = {
      standardizePath(raw.getBasePath)
    } ensuring goodPath

    private def standardizePath(path: String) = {
      StringUtils.stripEnd(path, "./")
    }
    def getRelativePath(file: VirtualFile): Option[String] = getRelativePath(file.getPath)
    def getRelativePath(path: String): Option[String] = {
      val base = getBasePath
      Seq(base, path).foreach(p => assume(goodPath(p)))
      if (hasParentPath(path, base)) {
        Some(StringUtils.removeStart(path, base)).filterNot(_.isEmpty).orElse(Some("/"))
      } else {
        None
      }
    }

    def getFileByRelative(path: String): Option[VirtualFile] = {
      assume(goodPath(path))
      Option(raw.getBaseDir.findFileByRelativePath(path))
    }
    def deleteFile(relativePath: String): Unit = {
      assume(goodPath(relativePath))
      getFileByRelative(relativePath).foreach(_.delete(this))
    }
    def deleteDir(relativePath: String): Unit = {
      assume(goodPath(relativePath))
      getFileByRelative(relativePath).foreach(_.delete(this))
    }
    def findOrCreateFile(relativePath: String): VirtualFile = {
      assume(goodPath(relativePath))
      val pathItems = relativePath.split("/")
      findOrCreateDir(pathItems.init.mkString("/")).findOrCreateChildData(this, pathItems.last)
    }
    def findOrCreateDir(relativePath: String): VirtualFile = {
      assume(goodPath(relativePath))
      relativePath.split("/").filter(_.length > 0).foldLeft(getBaseDir) {
        case (file, name) => Option(file.findChild(name)).fold(file.createChildDirectory(this, name))(identity)
      }
    }
    def getFileContent(file: VirtualFile): Content = {
      val charset = file.getCharset.name()
      Content(IOUtils.toString(file.getInputStream, charset), charset)
    }

    def getCachedFileContent(file: VirtualFile): Option[Content] = {
      Option(getDocumentManager.getCachedDocument(file)).map(_.getCharsSequence.toString).map(Content(_, file.getCharset.name()))
    }

    def containsFile(file: VirtualFile): Boolean = isSubPath(file.getPath, getBasePath)
    def getFileSummary(file: VirtualFile): Option[FileSummary] = getRelativePath(file).map(FileSummary(_, md5(getFileContent(file).text)))

    def smartGetFileContent(file: VirtualFile) = getCachedFileContent(file).getOrElse(getFileContent(file))

    def smartSetContentTo(path: String, content: Content): Unit = {
      val editors = getTextEditorsOfPath(path)
      if (editors.nonEmpty) {
        editors.foreach { editor =>
          val document = editor.getDocument
          val currentContent = document.getCharsSequence.toString
          val diffs = StringDiff.diffs(currentContent, content.text)
          diffs.foreach {
            case Insert(offset, newStr) => document.insertString(offset, newStr)
            case Delete(offset, length) => document.deleteString(offset, offset + length)
          }
        }
      } else {
        val file = findOrCreateFile(path)
        file.setBinaryContent(content.text.getBytes(content.charset))
      }
    }

  }

  trait ProjectContext {
    this: IdeaApiWrappers with IdeaEditorSupport with ProjectPathSupport =>

    @volatile private var _connection: Option[Connection] = None
    @volatile private var _serverStatus: Option[ServerStatusResponse] = None
    @volatile private var _clientInfo: Option[ClientInfoResponse] = None
    @volatile private var _server: Option[Server] = None

    def connection: Option[Connection] = _connection
    def connection_=(conn: Option[Connection]) = notifyChangesAfter(_connection = conn)

    def serverStatus: Option[ServerStatusResponse] = _serverStatus
    def serverStatus_=(status: Option[ServerStatusResponse]) = notifyChangesAfter(_serverStatus = status)

    def clientInfo: Option[ClientInfoResponse] = _clientInfo
    def clientInfo_=(info: Option[ClientInfoResponse]) = notifyChangesAfter(_clientInfo = info)

    def server: Option[Server] = _server
    def server_=(server: Option[Server]) = notifyChangesAfter(_server = server)

    def projectInfo: Option[ProjectInfoData] = for {
      server <- serverStatus
      client <- clientInfo
      p <- server.projects.find(_.name == client.project)
    } yield p

    def myClientId: Option[String] = clientInfo.map(_.clientId)
    def masterClientId: Option[String] = projectInfo.flatMap(_.clients.find(_.isMaster)).map(_.clientId)
    def allClientIds: Seq[String] = projectInfo.toSeq.flatMap(_.clients).map(_.clientId).toSeq
    def otherClientIds: Seq[String] = allClientIds.filterNot(Some(_) == myClientId)

    @deprecated()
    def watchingFiles: Seq[String] = projectInfo.map(_.watchingFiles).getOrElse(Nil)

    private def notifyChangesAfter(f: => Any): Unit = {
      f
      getMessageBus.foreach(ProjectStatusChanges.notify)
    }


  }

  trait IdeaApiWrappers {
    this: create =>
    def openFileDescriptor(file: VirtualFile) = new OpenFileDescriptor(raw, file)
    def fileEditorManager: FileEditorManager = FileEditorManager.getInstance(raw)
    def getStatusBar: StatusBar = WindowManager.getInstance().getStatusBar(raw)
    def getDocumentManager: FileDocumentManager = FileDocumentManager.getInstance()
    def getMessageBus: Option[MessageBus] = {
      if (raw.isDisposed) None else Some(raw.getMessageBus)
    }
    def createMessageConnection(): Option[MessageBusConnection] = {
      getMessageBus
        .map(_
        .connect(raw))
    }
    def getWindow = WindowManager.getInstance().getFrame(raw)
    def getComponent[T: ClassTag]: T = {
      val cls = implicitly[ClassTag[T]].runtimeClass
      raw.getComponent(cls).asInstanceOf[T]
    }
  }

  trait IdeaEditorSupport {
    this: IdeaApiWrappers with ProjectPathSupport =>
    def getAllTextEditors: Seq[Editor] = fileEditorManager.getAllEditors.toSeq.collect { case e: TextEditor => e}.map(_.getEditor)
    def getSelectedTextEditor: Option[Editor] = Option(fileEditorManager.getSelectedTextEditor)
    def pathOfSelectedTextEditor: Option[String] = getSelectedTextEditor.map(getFileOfEditor).flatMap(getRelativePath)
    def getTextEditorsOfPath(path: String): Seq[Editor] = {
      getEditorsOfPath(path).collect { case e: TextEditor => e}.map(_.getEditor)
    }
    def getFileOfEditor(editor: Editor): VirtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument)
    def getEditorsOfPath(path: String): Seq[FileEditor] = {
      getFileByRelative(path).map(file => fileEditorManager.getAllEditors(file).toSeq).getOrElse(Nil)
    }
    def getOpenedFiles: Seq[VirtualFile] = fileEditorManager.getOpenFiles.toSeq
  }


}


case class ServerAddress(ip: String, port: Int)

case class ClientVersionedDocuments(clientVersionedDocumentFactory: ClientVersionedDocumentFactory) {
  private var documents = Map.empty[String, ClientVersionedDocument]

  def get(path: String): ClientVersionedDocument = synchronized(documents(path))

  def find(path: String): Option[ClientVersionedDocument] = synchronized(documents.get(path))

  def getOrCreate(currentProject: RichProject, path: String): ClientVersionedDocument = synchronized {
    find(path) match {
      case Some(doc) => doc
      case _ => {
        val doc = clientVersionedDocumentFactory.create(path)
        documents += path -> doc
        doc
      }
    }
  }
}

case class Change(eventId: String, baseVersion: Int, diffs: Seq[ContentDiff])

case class FileTree(root: FileTreeNode)

case class FileTreeNode(data: FileTreeNodeData) extends DefaultMutableTreeNode(data) {
  override def hashCode(): Int = {
    data.file.hashCode()
  }

  override def equals(o: scala.Any): Boolean = o match {
    case d: DefaultMutableTreeNode => d.getUserObject match {
      case dd: FileTreeNodeData => dd.file == data.file
      case _ => false
    }
    case _ => false
  }
}

case class FileTreeNodeData(file: VirtualFile) {
  override def toString: String = file.getName
}



