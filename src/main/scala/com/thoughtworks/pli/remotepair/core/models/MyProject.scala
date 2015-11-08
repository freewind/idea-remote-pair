package com.thoughtworks.pli.remotepair.core.models

import com.intellij.openapi.fileEditor.{FileEditorManager, OpenFileDescriptor, TextEditor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.messages.MessageBus
import com.thoughtworks.pli.remotepair.core.{PluginLogger, ProjectStatusChanges}
import com.thoughtworks.pli.remotepair.idea.utils.Paths
import org.apache.commons.lang.StringUtils

class MyProject(val rawProject: Project)(ideaFactories: => IdeaFactories, logger: PluginLogger) {
  require(rawProject != null, "rawProject should not be null")

  implicit def myfile2idea(file: MyFile): MyFile = file.asInstanceOf[MyFile]

  def putUserData[T](key: DataKey[T], value: T, postAction: Option[() => Unit]): Unit = synchronized {
    rawProject.putUserData(IdeaKeys.get(key), value)
    postAction.foreach(_ ())
  }

  def getUserData[T](key: DataKey[T]): Option[T] = Option(rawProject.getUserData(IdeaKeys.get(key)))

  def getOrInitUserData[T](key: DataKey[T], initValue: T): T = synchronized {
    getUserData(key) match {
      case None => putUserData(key, initValue, None); initValue
      case Some(v) => v
    }
  }

  def baseDir: MyFile = ideaFactories(rawProject.getBaseDir)
  def openedFiles: Seq[MyFile] = fileEditorManager().getOpenFiles.toSeq.map(ideaFactories.apply)
  def openFileInTab(file: MyFile): Unit = file match {
    case f: MyFile =>
      val openFileDescriptor = new OpenFileDescriptor(rawProject, f.rawFile)
      if (openFileDescriptor.canNavigate) {
        openFileDescriptor.navigate(true)
      }
    case _ =>
  }
  def getRelativePath(fullPath: String): Option[String] = {
    val base = baseDir.path
    if (Paths.isSubPath(fullPath, base)) {
      Some(StringUtils.removeStart(fullPath, base)).filterNot(_.isEmpty).orElse(Some("/"))
    } else {
      logger.warn(s"$fullPath is not sub path of base: $base, can't get relative path")
      None
    }
  }
  def getFileByRelative(relativePath: String): Option[MyFile] = {
    val dir = baseDir
    Option(dir.rawFile.findFileByRelativePath(relativePath)).map(file => ideaFactories(file))
  }

  def findOrCreateDir(relativePath: String): MyFile = {
    relativePath.split("/").filter(_.length > 0).foldLeft(baseDir) {
      case (file, name) =>
        file.findChild(name).fold(file.createChildDirectory(name))(identity)
    }
  }

  def findOrCreateFile(relativePath: String): MyFile = {
    val pathItems = relativePath.split("/")
    val parentDir = findOrCreateDir(pathItems.init.mkString("/"))
    ideaFactories(parentDir.rawFile.findOrCreateChildData(this, pathItems.last))
  }

  def fileEditorManager() = FileEditorManager.getInstance(rawProject)
  def getTextEditorsOfPath(relativePath: String): Seq[MyEditor] = {
    getEditorsOfPath(relativePath).collect { case e: TextEditor => e }.map(textEditor => ideaFactories(textEditor.getEditor))
  }
  def getAllOpenedTextEditors: Seq[MyEditor] = openedFiles.flatMap(_.relativePath).flatMap(getTextEditorsOfPath)

  private def getEditorsOfPath(relativePath: String) = {
    getFileByRelative(relativePath).map(file => fileEditorManager().getAllEditors(file.rawFile).toSeq).getOrElse(Nil)
  }
  def notifyUserDataChanges(): Unit = {
    messageBus.foreach(ProjectStatusChanges.notify)
  }
  def window = WindowManager.getInstance().getFrame(rawProject)
  def statusBar = WindowManager.getInstance().getStatusBar(rawProject)
  def createMessageConnection() = {
    messageBus.map(_.connect(rawProject))
  }
  def messageBus: Option[MessageBus] = {
    if (rawProject.isDisposed) {
      logger.warn(s"$rawProject is disposed")
      None
    } else Some(rawProject.getMessageBus)
  }
  def showErrorDialog(title: String = "Error", message: String) {
    Messages.showMessageDialog(rawProject, message, title, Messages.getErrorIcon)
  }
  def showMessageDialog(message: String) = {
    Messages.showMessageDialog(rawProject, message, "Information", Messages.getInformationIcon)
  }
  def close(file: MyFile): Unit = fileEditorManager().closeFile(file.rawFile)
  def isOpened(file: MyFile): Boolean = fileEditorManager().isFileOpen(file.rawFile)
  def isActive(file: MyFile): Boolean = fileEditorManager().getSelectedFiles.contains(file.rawFile)

}
