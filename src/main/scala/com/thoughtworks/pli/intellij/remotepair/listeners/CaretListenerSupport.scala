package com.thoughtworks.pli.intellij.remotepair.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{CaretEvent, CaretListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.intellij.remotepair.{AppLogger, PublishEvents}

trait CaretListenerSupport extends PublishEvents with AppLogger {
  this: CurrentProjectHolder =>

  def createCaretListener(): ListenerManageSupport[CaretListener] = new ListenerManageSupport[CaretListener] {
    val key = new Key[CaretListener]("remote_pair.listeners.caret")

    def createNewListener(editor: Editor, file: VirtualFile, project: Project): CaretListener = new CaretListener {
      override def caretPositionChanged(e: CaretEvent) {
        log.info("########## caretPositionChanged: " + info(e))
        val event = MoveCaretEvent(currentProject.getRelativePath(file), e.getCaret.getOffset)
        publishEvent(event)
      }

      override def caretRemoved(e: CaretEvent) {
        log.info("########## caretRemoved: " + info(e))
      }

      override def caretAdded(e: CaretEvent) {
        log.info("######### caretAdded: " + info(e))
      }

      private def info(e: CaretEvent) = s"${e.getOldPosition} => ${e.getNewPosition}"
    }

    override def originRemoveListener(editor: Editor): (CaretListener) => Any = editor.getCaretModel.removeCaretListener

    override def originAddListener(editor: Editor): (CaretListener) => Any = editor.getCaretModel.addCaretListener
  }


}
