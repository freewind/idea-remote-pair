package com.thoughtworks.pli.intellij.remotepair.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{CaretEvent, CaretListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.{MoveCaretEvent, PublishEvents}

trait CaretListenerSupport extends PublishEvents {
  this: CurrentProjectHolder =>

  def createCaretListener(): ListenerManageSupport[CaretListener] = new ListenerManageSupport[CaretListener] {
    val key = new Key[CaretListener]("remote_pair.listeners.caret")

    def createNewListener(editor: Editor, file: VirtualFile, project: Project): CaretListener = new CaretListener {
      override def caretPositionChanged(e: CaretEvent) {
        println("########## caretPositionChanged: " + info(e))
        val event = MoveCaretEvent(currentProject.getRelativePath(file), e.getCaret.getOffset)
        publishEvent(event)
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


}
