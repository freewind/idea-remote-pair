package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.openapi.fileEditor.{FileEditorManager, OpenFileDescriptor, TextEditor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.thoughtworks.pli.remotepair.core.models.{MyEditor, MyFile, MyProject}
import com.thoughtworks.pli.remotepair.idea.utils.Paths
import org.apache.commons.lang.StringUtils

private[idea] class IdeaProjectImpl(val rawProject: Project)(ideaFactories: => IdeaFactories) extends MyProject {
  require(rawProject != null, "rawProject should not be null")

  override def putUserData[T](key: Key[T], value: T): Unit = rawProject.putUserData(key, value)
  override def getUserData[T](key: Key[T]): T = rawProject.getUserData(key)
  override def getBaseDir: IdeaFileImpl = ideaFactories(rawProject.getBaseDir)
  override def getComponent[T](interfaceClass: Class[T]): T = rawProject.getComponent(interfaceClass)
  override def getOpenedFiles(): Seq[IdeaFileImpl] = fileEditorManager().getOpenFiles.toSeq.map(ideaFactories.apply)
  override def openFileInTab(file: MyFile): Unit = file match {
    case f: IdeaFileImpl =>
      val openFileDescriptor = new OpenFileDescriptor(rawProject, f.rawFile)
      if (openFileDescriptor.canNavigate) {
        openFileDescriptor.navigate(true)
      }
    case _ =>
  }
  override def getRelativePath(fullPath: String): Option[String] = {
    val base = getBaseDir.path
    if (Paths.isSubPath(fullPath, base)) {
      Some(StringUtils.removeStart(fullPath, base)).filterNot(_.isEmpty).orElse(Some("/"))
    } else {
      None
    }
  }
  override def getFileByRelative(relativePath: String): Option[IdeaFileImpl] = {
    val dir = getBaseDir
    Option(ideaFactories(dir.rawFile.findFileByRelativePath(relativePath)))
  }

  override def findOrCreateDir(relativePath: String): IdeaFileImpl = {
    relativePath.split("/").filter(_.length > 0).foldLeft(getBaseDir) {
      case (file, name) =>
        file.findChild(name).fold(file.createChildDirectory(name))(identity)
    }
  }

  def findOrCreateFile(relativePath: String): IdeaFileImpl = {
    val pathItems = relativePath.split("/")
    val parentDir = findOrCreateDir(pathItems.init.mkString("/"))
    ideaFactories(parentDir.rawFile.findOrCreateChildData(this, pathItems.last))
  }

  def fileEditorManager() = FileEditorManager.getInstance(rawProject)
  override def getTextEditorsOfPath(relativePath: String): Seq[MyEditor] = {
    getEditorsOfPath(relativePath).collect { case e: TextEditor => e }.map(textEditor => ideaFactories(textEditor.getEditor))
  }
  private def getEditorsOfPath(relativePath: String) = {
    getFileByRelative(relativePath).map(file => fileEditorManager().getAllEditors(file.rawFile).toSeq).getOrElse(Nil)
  }
}
