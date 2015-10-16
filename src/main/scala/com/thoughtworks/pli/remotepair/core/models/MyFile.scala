package com.thoughtworks.pli.remotepair.core.models

import com.intellij.openapi.fileEditor.{FileDocumentManager, FileEditorManager}
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{Content, FileSummary}
import com.thoughtworks.pli.remotepair.core.MyUtils
import com.thoughtworks.pli.remotepair.idea.utils.Paths
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

class MyFile(val rawFile: VirtualFile, val project: MyProject)(myUtils: MyUtils, ideaFactories: IdeaFactories) {
  require(rawFile != null, "rawFile should not be null")

  def exists: Boolean = rawFile.exists
  def documentContent: Option[Content] = {
    val document = FileDocumentManager.getInstance().getDocument(rawFile)
    Option(document).map(_.getCharsSequence.toString).map(Content(_, rawFile.getCharset.name()))
  }
  def isBinary = rawFile.getFileType.isBinary
  def name: String = rawFile.getName
  def findChild(name: String): Option[MyFile] = Option(ideaFactories(rawFile.findChild(name)))
  def delete(): Unit = rawFile.delete(this)
  def isDirectory: Boolean = rawFile.isDirectory
  def content: Content = {
    val charset = rawFile.getCharset.name()
    Content(IOUtils.toString(rawFile.getInputStream, charset), charset)
  }
  def setContent(newContent: String): Unit = rawFile.setBinaryContent(content.text.getBytes(content.charset))
  def path: String = StringUtils.stripEnd(rawFile.getPath, "./")
  def createChildDirectory(name: String): MyFile = ideaFactories(rawFile.createChildDirectory(this, name))
  def children: Seq[MyFile] = rawFile.getChildren.map(ideaFactories.apply)
  def parent: MyFile = ideaFactories(rawFile.getParent)
  def move(newParent: MyFile): Unit = newParent match {
    case p: MyFile => rawFile.move(this, p.rawFile)
  }
  def rename(newName: String): Unit = rawFile.rename(this, newName)
  def isChildOf(parent: MyFile): Boolean = Paths.isSubPath(this.path, parent.path)
  def relativePath: Option[String] = project.getRelativePath(path)
  private def fileEditorManager() = FileEditorManager.getInstance(project.rawProject)
  def summary: Option[FileSummary] = relativePath.map(FileSummary(_, myUtils.md5(content.text)))
}
