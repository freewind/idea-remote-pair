package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{CaretAdapter, CaretEvent, CaretListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.idea.core._

class ProjectCaretListenerFactory(publishEvent: PublishEvent, logger: Logger, inWatchingList: InWatchingList, getDocumentContent: GetDocumentContent, getUserData: GetUserData, putUserData: PutUserData, getRelativePath: GetRelativePath, getCaretOffset: GetCaretOffset, isReadonlyMode: IsReadonlyMode)
  extends ListenerManager[CaretListener] {

  val key = new Key[CaretListener]("remote_pair.listeners.caret")

  val KeyDocumentLength = new Key[Int]("remote_pair.listeners.caret.doc_length")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): CaretListener = new CaretAdapter {

    override def caretPositionChanged(e: CaretEvent): Unit = if (inWatchingList(file) && !isReadonlyMode()) {
      logger.info("########## caretPositionChanged: " + info(e))
      val docLength = getDocumentContent(editor).length()
      if (getUserData(editor, KeyDocumentLength).contains(docLength)) {
        for {
          path <- getRelativePath(file)
          event = MoveCaretEvent(path, getCaretOffset(e))
        } publishEvent(event)
      } else {
        putUserData(editor, KeyDocumentLength, docLength)
      }
    }

    private def info(e: CaretEvent) = s"${e.getOldPosition} => ${e.getNewPosition}"
  }

  override def originRemoveListener(editor: Editor): (CaretListener) => Any = editor.getCaretModel.removeCaretListener

  override def originAddListener(editor: Editor): (CaretListener) => Any = editor.getCaretModel.addCaretListener

}
