package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{CaretEvent, CaretListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.PublishEvent

case class ProjectCaretListener(currentProject: RichProject, publishEvent: PublishEvent, logger: Logger) extends ListenerManageSupport[CaretListener] {

  val key = new Key[CaretListener]("remote_pair.listeners.caret")

  private val keyDocumentLength = new Key[Int]("remote_pair.listeners.caret.doc_length")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): CaretListener = new CaretListener {
    override def caretPositionChanged(e: CaretEvent) {
      logger.info("########## caretPositionChanged: " + info(e))

      val thisLength = editor.getDocument.getCharsSequence.length()
      if (thisLength == editor.getUserData(keyDocumentLength)) {
        for {
          path <- currentProject.getRelativePath(file)
          event = MoveCaretEvent(path, e.getCaret.getOffset)
        } publishEvent(event)
      } else {
        editor.putUserData(keyDocumentLength, thisLength)
      }
    }

    override def caretRemoved(e: CaretEvent) {
      logger.info("########## caretRemoved: " + info(e))
    }

    override def caretAdded(e: CaretEvent) {
      logger.info("######### caretAdded: " + info(e))
    }

    private def info(e: CaretEvent) = s"${e.getOldPosition} => ${e.getNewPosition}"
  }

  override def originRemoveListener(editor: Editor): (CaretListener) => Any = editor.getCaretModel.removeCaretListener

  override def originAddListener(editor: Editor): (CaretListener) => Any = editor.getCaretModel.addCaretListener

}
