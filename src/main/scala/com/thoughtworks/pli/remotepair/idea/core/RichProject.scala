package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.{StatusBar, WindowManager}
import com.intellij.util.messages.{MessageBus, MessageBusConnection}
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.intellij.remotepair.utils._
import com.thoughtworks.pli.remotepair.idea.core.ConnectionFactory.Connection
import com.thoughtworks.pli.remotepair.idea.core.MyChannelHandlerFactory.MyChannelHandler
import com.thoughtworks.pli.remotepair.idea.core.tree.{CreateFileTree, FileTreeNode}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

import scala.reflect.ClassTag


case class ServerAddress(ip: String, port: Int)

class IsFileOpened(getFileEditorManager: GetFileEditorManager) {
  def apply(file: VirtualFile) = getFileEditorManager().isFileOpen(file)
}

class GetOpenedFiles(getFileEditorManager: GetFileEditorManager) {
  def apply(): Seq[VirtualFile] = getFileEditorManager().getOpenFiles.toSeq
}

class GetEditorPath(getFileOfEditor: GetFileOfEditor, getRelativePath: GetRelativePath) {
  def apply(editor: Editor): Option[String] = getRelativePath(getFileOfEditor(editor))
}

class GetFileOfEditor() {
  def apply(editor: Editor): VirtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument)
}

class GetSelectedTextEditor(getFileEditorManager: GetFileEditorManager) {
  def apply(): Option[Editor] = Option(getFileEditorManager().getSelectedTextEditor)
}

class GetAllTextEditors(getAllEditors: GetAllEditors) {
  def apply: Seq[Editor] = getAllEditors().collect { case e: TextEditor => e}.map(_.getEditor)
}

class GetAllEditors(getFileEditorManager: GetFileEditorManager) {
  def apply(): Seq[FileEditor] = getFileEditorManager().getAllEditors.toSeq
}

object ClientVersionedDocuments {
  val Key = new Key[Map[String, ClientVersionedDocument]](ClientVersionedDocuments.getClass.getName)
}
class ClientVersionedDocuments(clientVersionedDocumentFactory: ClientVersionedDocument.Factory, currentProjectScope: CurrentProjectScope) {
  private val documents = currentProjectScope.value(ClientVersionedDocuments.Key, Map.empty[String, ClientVersionedDocument])

  def get(path: String): ClientVersionedDocument = synchronized(documents.get(path))

  def find(path: String): Option[ClientVersionedDocument] = synchronized(documents.get.get(path))

  def create(event: CreateDocumentConfirmation): ClientVersionedDocument = synchronized {
    val doc = clientVersionedDocumentFactory.apply(event)
    documents.set(documents.get + (doc.path -> doc))
    doc
  }
}

case class Change(eventId: String, baseVersion: Int, diffs: Seq[ContentDiff])


class GetEditorsOfPath(getFileByRelative: GetFileByRelative, getFileEditorManager: GetFileEditorManager) {

  def apply(path: String): Seq[FileEditor] = {
    getFileByRelative(path).map(file => getFileEditorManager().getAllEditors(file).toSeq).getOrElse(Nil)
  }
}

class GetTextEditorsOfPath(getEditorsOfPath: GetEditorsOfPath) {
  def apply(path: String): Seq[Editor] = {
    getEditorsOfPath(path).collect { case e: TextEditor => e}.map(_.getEditor)
  }
}

class FindOrCreateFile(currentProject: Project, findOrCreateDir: FindOrCreateDir, runtimeAssertions: RuntimeAssertions) {

  import runtimeAssertions._

  def apply(relativePath: String): VirtualFile = {
    assume(goodPath(relativePath))
    val pathItems = relativePath.split("/")
    findOrCreateDir(pathItems.init.mkString("/")).findOrCreateChildData(this, pathItems.last)
  }
}

class GetProjectBasePath(getProjectBaseDir: GetProjectBaseDir, standardizePath: StandardizePath) {
  def apply(): String = standardizePath(getProjectBaseDir().getPath)
}

class GetProjectBaseDir(currentProject: Project) {
  def apply(): VirtualFile = currentProject.getBaseDir
}

class StandardizePath {
  def apply(path: String) = {
    StringUtils.stripEnd(path, "./")
  }
}

class CreateChildDirectory {
  def apply(file: VirtualFile, newDirName: String): VirtualFile = {
    file.createChildDirectory(this, newDirName)
  }
}

class FindChild {
  def apply(file: VirtualFile, name: String): Option[VirtualFile] = {
    Option(file.findChild(name))
  }
}

class FindOrCreateDir(runtimeAssertions: RuntimeAssertions, getProjectBaseDir: GetProjectBaseDir, createChildDirectory: CreateChildDirectory, findChild: FindChild) {

  def apply(relativePath: String): VirtualFile = {
    relativePath.split("/").filter(_.length > 0).foldLeft(getProjectBaseDir()) {
      case (file, name) =>
        findChild(file, name).fold(createChildDirectory(file, name))(identity)
    }
  }
}

class WriteToProjectFile(getTextEditorsOfPath: GetTextEditorsOfPath, findOrCreateFile: FindOrCreateFile) {
  def apply(path: String, content: Content): Unit = {
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


class GetAllWatchingFiles(getProjectBaseDir: GetProjectBaseDir, getServerWatchingFiles: GetServerWatchingFiles, createFileTree: CreateFileTree, isInPathList: IsInPathList) {
  def apply(): Seq[VirtualFile] = {
    val tree = createFileTree(getProjectBaseDir(), isInPathList(_, getServerWatchingFiles()))
    toList(tree).filterNot(_.isDirectory).filterNot(_.getFileType.isBinary)
  }

  private def toList(tree: FileTreeNode): List[VirtualFile] = {
    def fetchChildren(node: FileTreeNode, result: List[VirtualFile]): List[VirtualFile] = {
      if (node.getChildCount == 0) result
      else {
        val nodes = (0 until node.getChildCount).map(node.getChildAt).map(_.asInstanceOf[FileTreeNode]).toList
        nodes.foldLeft(nodes.map(_.data.file) ::: result) {
          case (all, child) => fetchChildren(child, all)
        }
      }
    }
    fetchChildren(tree, Nil)
  }

}

class DeleteProjectDir(runtimeAssertions: RuntimeAssertions, getFileByRelative: GetFileByRelative) {

  import runtimeAssertions.goodPath

  def apply(relativePath: String): Unit = {
    assume(goodPath(relativePath))
    getFileByRelative(relativePath).foreach(_.delete(this))
  }
}

class GetFileByRelative(runtimeAssertions: RuntimeAssertions, getProjectBaseDir: GetProjectBaseDir) {

  import runtimeAssertions.goodPath

  def apply(path: String): Option[VirtualFile] = {
    assume(goodPath(path))
    Option(getProjectBaseDir().findFileByRelativePath(path))
  }

}

class GetWatchingFileSummaries(getAllWatchingFiles: GetAllWatchingFiles, getFileSummary: GetFileSummary) {
  def apply(): Seq[FileSummary] = getAllWatchingFiles().flatMap(getFileSummary.apply)
}

class GetFileSummary(getRelativePath: GetRelativePath, getFileContent: GetFileContent, md5: Md5) {
  def apply(file: VirtualFile): Option[FileSummary] = getRelativePath(file).map(FileSummary(_, md5(getFileContent(file).text)))
}

class GetFileContent {
  def apply(file: VirtualFile): Content = {
    val charset = file.getCharset.name()
    Content(IOUtils.toString(file.getInputStream, charset), charset)
  }
}

class DeleteProjectFile(getFileByRelative: GetFileByRelative, runtimeAssertions: RuntimeAssertions) {

  import runtimeAssertions.goodPath

  def apply(relativePath: String): Unit = {
    assume(goodPath(relativePath))
    getFileByRelative(relativePath).foreach(_.delete(this))
  }
}

class GetCachedFileContent(getFileContent: GetFileContent, getDocumentContent: GetDocumentContent) {
  def apply(file: VirtualFile): Option[Content] = {
    val cachedDocument = FileDocumentManager.getInstance().getCachedDocument(file)
    Option(cachedDocument).map(getDocumentContent.apply).map(Content(_, file.getCharset.name()))
  }
}

class GetFileEditorManager(currentProject: Project) {
  def apply(): FileEditorManager = FileEditorManager.getInstance(currentProject)
}
class ShowMessageDialog(currentProject: Project) {
  def apply(message: String) = {
    Messages.showMessageDialog(currentProject, message, "Information", Messages.getInformationIcon)
  }
}

class ShowErrorDialog(currentProject: Project) {
  def apply(title: String = "Error", message: String) = {
    Messages.showMessageDialog(currentProject, message, title, Messages.getErrorIcon)
  }
}

class ContainsProjectFile(getProjectBasePath: GetProjectBasePath, isSubPath: IsSubPath) {
  def apply(file: VirtualFile): Boolean = isSubPath(file.getPath, getProjectBasePath())
}
class GetOpenFileDescriptor(currentProject: Project) {
  def apply(file: VirtualFile) = new OpenFileDescriptor(currentProject, file)
}

class GetStatusBar(currentProject: Project) {
  def apply(): StatusBar = WindowManager.getInstance().getStatusBar(currentProject)
}
class GetMessageBus(currentProject: Project) {
  def apply(): Option[MessageBus] = {
    if (currentProject.isDisposed) None else Some(currentProject.getMessageBus)
  }
}
class GetProjectWindow(currentProject: Project) {
  def apply() = WindowManager.getInstance().getFrame(currentProject)
}
class GetComponent(currentProject: Project) {
  def apply[T: ClassTag](): T = {
    val cls = implicitly[ClassTag[T]].runtimeClass
    currentProject.getComponent(cls).asInstanceOf[T]
  }
}
class GetDocumentManager {
  def apply(): FileDocumentManager = FileDocumentManager.getInstance()
}

class CreateMessageConnection(getMessageBus: GetMessageBus, currentProject: Project) {
  def apply(): Option[MessageBusConnection] = {
    getMessageBus().map(_.connect(currentProject))
  }
}

object ConnectionHolder {
  val Key = new Key[Option[Connection]](ConnectionHolder.getClass.getName)
}

class ConnectionHolder(notifyChanges: NotifyChanges, currentProjectScope: CurrentProjectScope) {
  private val connection = currentProjectScope.value(ConnectionHolder.Key, None)
  def get: Option[Connection] = connection.get
  def put(conn: Option[Connection]) = {
    connection.set(conn)
    notifyChanges()
  }
}

object ServerStatusHolder {
  val Key = new Key[Option[ServerStatusResponse]](ServerStatusHolder.getClass.getName)
}

class ServerStatusHolder(notifyChanges: NotifyChanges, currentProjectScope: CurrentProjectScope) {
  private val serverStatus = currentProjectScope.value(ServerStatusHolder.Key, None)
  def get: Option[ServerStatusResponse] = serverStatus.get
  def put(status: Option[ServerStatusResponse]) = {
    serverStatus.set(status)
    notifyChanges
  }

}

object ClientInfoHolder {
  val Key = new Key[Option[ClientInfoResponse]](ClientInfoHolder.getClass.getName)
}

class ClientInfoHolder(notifyChanges: NotifyChanges, currentProjectScope: CurrentProjectScope) {
  private val clientInfo = currentProjectScope.value(ClientInfoHolder.Key, None)
  def get: Option[ClientInfoResponse] = clientInfo.get
  def put(info: Option[ClientInfoResponse]) = {
    clientInfo.set(info)
    notifyChanges()
  }

}

object ServerHolder {
  val Key = new Key[Option[Server]](ServerHolder.getClass.getName)
}

class ServerHolder(notifyChanges: NotifyChanges, currentProjectScope: CurrentProjectScope) {
  private val server = currentProjectScope.value(ServerHolder.Key, None)
  def get: Option[Server] = server.get
  def put(server: Option[Server]) = {
    this.server.set(server)
    notifyChanges()
  }
}
class GetProjectInfoData(serverStatusHolder: ServerStatusHolder, clientInfoHolder: ClientInfoHolder) {

  def apply(): Option[ProjectInfoData] = for {
    server <- serverStatusHolder.get
    client <- clientInfoHolder.get
    p <- server.projects.find(_.name == client.project)
  } yield p

}

class GetOtherClients(getAllClients: GetAllClients, getMyClientId: GetMyClientId) {
  def apply(): Seq[ClientInfoResponse] = getAllClients().filterNot(client => Some(client.clientId) == getMyClientId())
}

class GetAllClients(getProjectInfoData: GetProjectInfoData) {
  def apply(): Seq[ClientInfoResponse] = getProjectInfoData().toSeq.flatMap(_.clients).toSeq
}

class GetMasterClient(getProjectInfoData: GetProjectInfoData) {
  def apply(): Option[ClientInfoResponse] = getProjectInfoData().flatMap(_.clients.find(_.isMaster))
}


class GetMasterClientId(getProjectInfoData: GetProjectInfoData) {
  def apply(): Option[String] = getProjectInfoData().flatMap(_.clients.find(_.isMaster)).map(_.clientId)
}

object ChannelHandlerHolder {
  val Key = new Key[Option[MyChannelHandler]](ChannelHandlerHolder.getClass.getName)
}

class ChannelHandlerHolder(currentProjectScope: CurrentProjectScope) {
  private val channelHandler = currentProjectScope.value(ChannelHandlerHolder.Key, None)
  def get: Option[MyChannelHandler] = channelHandler.get
  def put(handler: Option[MyChannelHandler]) = {
    channelHandler.set(handler)
  }
}
