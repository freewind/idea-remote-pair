package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.{StatusBar, WindowManager}
import com.intellij.util.messages.MessageBus
import com.thoughtworks.pli.intellij.remotepair.RuntimeAssertions._
import com.thoughtworks.pli.intellij.remotepair.client.doc.ClientVersionedDocument
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.intellij.remotepair.utils._
import io.netty.channel.ChannelHandlerContext
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

import scala.reflect.ClassTag

case class RichProject(raw: Project) extends ProjectContext with IdeaApiWrappers with IdeaEditorSupport with ProjectPathSupport with IdeaMessageDialogs with ProjectFilesSupport with VersionedDocuments {
  def getName = raw.getName
}

trait IdeaApiWrappers {
  this: RichProject =>
  def openFileDescriptor(file: VirtualFile) = new OpenFileDescriptor(raw, file)
  def fileEditorManager: FileEditorManager = FileEditorManager.getInstance(raw)
  def getStatusBar: StatusBar = WindowManager.getInstance().getStatusBar(raw)
  def getDocumentManager: FileDocumentManager = FileDocumentManager.getInstance()
  def getMessageBus: MessageBus = raw.getMessageBus
  def createMessageConnection() = getMessageBus.connect(raw)
  def getComponent[T: ClassTag]: T = {
    val cls = implicitly[ClassTag[T]].runtimeClass
    raw.getComponent(cls).asInstanceOf[T]
  }
}

trait ProjectPathSupport extends Md5Support {
  this: RichProject =>
  def getBaseDir: VirtualFile = raw.getBaseDir
  def getBasePath: String = {
    standardizePath(raw.getBasePath)
  } ensuring goodPath

  private def standardizePath(path: String) = {
    StringUtils.stripEnd(path, "./")
  }
  def getRelativePath(file: VirtualFile): String = getRelativePath(file.getPath)
  def getRelativePath(path: String): String = {
    val base = getBasePath

    Seq(base, path).foreach(p => assume(goodPath(p)))
    require(hasParentPath(path, base))

    Some(StringUtils.removeStart(path, base)).filterNot(_.isEmpty).getOrElse("/")
  } ensuring goodPath

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

  def containsFile(file: VirtualFile): Boolean = PathUtils.isSubPathOf(file.getPath, getBasePath)
  def getFileSummary(file: VirtualFile) = {
    FileSummary(getRelativePath(file), md5(getFileContent(file).text))
  }

  def smartGetFileContent(file: VirtualFile) = getCachedFileContent(file).getOrElse(getFileContent(file))

  def smartSetContentTo(path: String, content: Content): Unit = {
    val editors = getTextEditorsOfPath(path)
    if (editors.nonEmpty) {
      editors.foreach { editor =>
        val document = editor.getEditor.getDocument
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

trait IdeaEditorSupport {
  this: IdeaApiWrappers with ProjectPathSupport =>
  def getSelectedTextEditor: Option[Editor] = Option(fileEditorManager.getSelectedTextEditor)
  def pathOfSelectedTextEditor: Option[String] = getSelectedTextEditor
    .map(editor => FileDocumentManager.getInstance().getFile(editor.getDocument))
    .map(getRelativePath)
  def getTextEditorsOfPath(path: String): Seq[TextEditor] = {
    getEditorsOfPath(path).collect { case e: TextEditor => e}
  }
  def getEditorsOfPath(path: String): Seq[FileEditor] = {
    getFileByRelative(path).map(file => fileEditorManager.getAllEditors(file).toSeq).getOrElse(Nil)
  }
  def getOpenedFiles: Seq[VirtualFile] = fileEditorManager.getOpenFiles.toSeq
}

trait IdeaMessageDialogs {
  this: RichProject =>
  def showMessageDialog(message: String) = {
    Messages.showMessageDialog(raw, message, "Information", Messages.getInformationIcon)
  }

  def showErrorDialog(title: String, message: String) = {
    Messages.showMessageDialog(raw, message, title, Messages.getErrorIcon)
  }
}

trait ProjectContext {
  this: IdeaApiWrappers with IdeaEditorSupport with ProjectPathSupport =>

  private var _context: Option[ChannelHandlerContext] = None
  private var _serverStatus: Option[ServerStatusResponse] = None
  private var _clientInfo: Option[ClientInfoResponse] = None
  private var _server: Option[Server] = None

  def context: Option[ChannelHandlerContext] = _context
  def context_=(ctx: Option[ChannelHandlerContext]) = notifyChangesAfter(_context = ctx)

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

  def ignoredFiles = projectInfo.map(_.ignoredFiles).getOrElse(Nil)

  private def notifyChangesAfter(f: => Any): Unit = {
    f
    ProjectStatusChanges.notify(getMessageBus)
  }
}

trait VersionedDocuments {
  val versionedDocuments = new Documents
}

class Documents {
  private var documents = Map.empty[String, ClientVersionedDocument]

  def get(path: String): ClientVersionedDocument = synchronized(documents(path))

  def find(path: String): Option[ClientVersionedDocument] = synchronized(documents.get(path))

  def getOrCreate(currentProject: RichProject, path: String) = synchronized {
    find(path) match {
      case Some(doc) => doc
      case _ => {
        val doc = new ClientVersionedDocument(currentProject, path)
        documents += path -> doc
        doc
      }
    }
  }
}

case class Change(eventId: String, baseVersion: Int, diffs: Seq[ContentDiff])



