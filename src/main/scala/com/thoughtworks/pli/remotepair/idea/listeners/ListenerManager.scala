package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.util.{UserDataHolder, Key}
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.project.Project
import java.util.EventListener

trait ListenerManager[T <: EventListener] {

  val key: Key[T]

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): T

  def originAddListener(editor: Editor): T => Any

  def originRemoveListener(editor: Editor): T => Any

  def getListener(editor: UserDataHolder): Option[T] = {
    Option(editor.getUserData(key))
  }

  def putListener(editor: UserDataHolder, listener: T) {
    editor.putUserData(key, listener)
  }

  def removeListener(editor: Editor) {
    getListener(editor).foreach { listener =>
      originRemoveListener(editor)(listener)
      editor.putUserData(key.asInstanceOf[Key[Any]], null)
    }
  }

  def addListener(editor: Editor, file: VirtualFile, project: Project) {
    getListener(editor) match {
      case None =>
        val listener = createNewListener(editor, file, project)
        originAddListener(editor)(listener)
        putListener(editor, listener)
      case _ =>
    }
  }

}
