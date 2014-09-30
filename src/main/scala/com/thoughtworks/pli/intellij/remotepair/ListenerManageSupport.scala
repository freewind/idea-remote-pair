package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.util.{UserDataHolder, Key}
import com.intellij.openapi.editor.Editor

trait ListenerManageSupport[T] {

  val key: Key[T]

  def createNewListener(): T

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

  def addListener(editor: Editor) {
    getListener(editor) match {
      case None =>
        val listener = createNewListener()
        originAddListener(editor)(listener)
        putListener(editor, listener)
      case _ =>
    }
  }

}
