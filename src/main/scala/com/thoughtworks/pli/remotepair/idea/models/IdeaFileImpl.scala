package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.openapi.fileEditor.{FileDocumentManager, FileEditorManager}
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{Content, FileSummary}
import com.thoughtworks.pli.remotepair.core.MyUtils
import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.remotepair.idea.utils.Paths
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

private[idea] class IdeaFileImpl(val rawFile: VirtualFile, val project: IdeaProjectImpl)(myUtils: MyUtils, ideaFactories: IdeaFactories) extends MyFile {
  require(rawFile != null, "rawFile should not be null")

  override def exists: Boolean = rawFile.exists
  override def documentContent: Option[Content] = {
    val document = FileDocumentManager.getInstance().getDocument(rawFile)
    Option(document).map(_.getCharsSequence.toString).map(Content(_, rawFile.getCharset.name()))
  }
  override def isBinary = rawFile.getFileType.isBinary
  override def name: String = rawFile.getName
  override def findChild(name: String): Option[IdeaFileImpl] = Option(ideaFactories(rawFile.findChild(name)))
  override def delete(): Unit = rawFile.delete(this)
  override def isDirectory: Boolean = rawFile.isDirectory
  override def content: Content = {
    val charset = rawFile.getCharset.name()
    Content(IOUtils.toString(rawFile.getInputStream, charset), charset)
  }
  override def setContent(newContent: String): Unit = rawFile.setBinaryContent(content.text.getBytes(content.charset))
  override def path: String = StringUtils.stripEnd(rawFile.getPath, "./")
  override def createChildDirectory(name: String): IdeaFileImpl = ideaFactories(rawFile.createChildDirectory(this, name))
  override def children: Seq[MyFile] = rawFile.getChildren.map(ideaFactories.apply)
  override def parent: MyFile = ideaFactories(rawFile.getParent)
  override def move(newParent: MyFile): Unit = newParent match {
    case p: IdeaFileImpl => rawFile.move(this, p.rawFile)
  }
  override def rename(newName: String): Unit = rawFile.rename(this, newName)
  override def isChildOf(parent: MyFile): Boolean = Paths.isSubPath(this.path, parent.path)
  override def relativePath: Option[String] = project.getRelativePath(path)
  private def fileEditorManager() = FileEditorManager.getInstance(project.rawProject)
  override def summary: Option[FileSummary] = relativePath.map(FileSummary(_, myUtils.md5(content.text)))
}
