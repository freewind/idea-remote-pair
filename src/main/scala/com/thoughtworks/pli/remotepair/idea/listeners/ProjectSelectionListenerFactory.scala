package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{SelectionEvent, SelectionListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.idea.core._

case class ProjectSelectionListenerFactory(publishEvent: PublishEvent, logger: Logger, inWatchingList: InWatchingList, getRelativePath: GetRelativePath, getSelectionEventInfo: GetSelectionEventInfo)
  extends ListenerManager[SelectionListener] {

  val key = new Key[SelectionListener]("remote_pair.listeners.selection")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): SelectionListener = new SelectionListener {

    override def selectionChanged(e: SelectionEvent): Unit = if (inWatchingList(file)) {
      logger.info("####### selectionChanged: " + getSelectionEventInfo(e))
      for {
        path <- getRelativePath(file)
        range = e.getNewRange
        event = SelectContentEvent(path, range.getStartOffset, range.getEndOffset - range.getStartOffset)
      } publishEvent(event)
    }
  }

  override def originAddListener(editor: Editor): (SelectionListener) => Any = editor.getSelectionModel.addSelectionListener

  override def originRemoveListener(editor: Editor): (SelectionListener) => Any = editor.getSelectionModel.removeSelectionListener

}

