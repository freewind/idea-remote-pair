package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{CaretEvent, CaretListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.intellij.remotepair.PublishEvents
import com.thoughtworks.pli.remotepair.idea.core.{PublishEvents, AppLogger, CurrentProjectHolder}

trait CaretListenerSupport extends PublishEvents with AppLogger {
  this: CurrentProjectHolder =>

  def createCaretListener(): ListenerManageSupport[CaretListener] = new ListenerManageSupport[CaretListener] {
    val key = new Key[CaretListener]("remote_pair.listeners.caret")

    private val keyDocumentLength = new Key[Int]("remote_pair.listeners.caret.doc_length")

    def createNewListener(editor: Editor, file: VirtualFile, project: Project): CaretListener = new CaretListener {
      override def caretPositionChanged(e: CaretEvent) {
        log.info("########## caretPositionChanged: " + info(e))

        val thisLength = editor.getDocument.getCharsSequence.length()
        if (thisLength == editor.getUserData(keyDocumentLength)) {
          val path = currentProject.getRelativePath(file)
          val event = MoveCaretEvent(path, e.getCaret.getOffset)
          publishEvent(event)
        } else {
          editor.putUserData(keyDocumentLength, thisLength)
        }
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
