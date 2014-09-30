package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.editor.event.{CaretEvent, CaretListener}
import com.intellij.openapi.util.Key
import com.intellij.openapi.editor.Editor

class CaretListenerSupport extends ListenerManageSupport[CaretListener] {

  val key = new Key[CaretListener]("remote_pair.listeners.caret")

  override def createNewListener(): CaretListener = new CaretListener {
    override def caretPositionChanged(e: CaretEvent) {
      println("########## caretPositionChanged: " + info(e))
    }

    override def caretRemoved(e: CaretEvent) {
      println("########## caretRemoved: " + info(e))
    }

    override def caretAdded(e: CaretEvent) {
      println("######### caretAdded: " + info(e))
    }

    private def info(e: CaretEvent) = s"${e.getOldPosition} => ${e.getNewPosition}"
  }

  override def originRemoveListener(editor: Editor): (CaretListener) => Any = editor.getCaretModel.removeCaretListener

  override def originAddListener(editor: Editor): (CaretListener) => Any = editor.getCaretModel.addCaretListener

}
