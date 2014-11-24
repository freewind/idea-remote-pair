package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.{FileDocumentManager, FileEditorManager, OpenFileDescriptor, TextEditor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.{StatusBar, WindowManager}

import scala.reflect.ClassTag

case class RichProject(raw: Project) {

  def getName = raw.getName
  def getComponent[T: ClassTag]: T = {
    val cls = implicitly[ClassTag[T]].runtimeClass
    raw.getComponent(cls).asInstanceOf[T]
  }

  def openFileDescriptor(file: VirtualFile) = new OpenFileDescriptor(raw, file)

  def getByRelative(path: String): Option[VirtualFile] = Option(raw.getBaseDir.findFileByRelativePath(path))

  def getTextEditorsOfPath(path: String): Seq[TextEditor] = {
    getFile(path).map(file => fileEditorManager.getAllEditors(file).toSeq.collect { case e: TextEditor => e}).getOrElse(Nil)
  }
  def getFile(path: String): Option[VirtualFile] = {
    Option(raw.getBaseDir.findFileByRelativePath(path))
  }
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
}
