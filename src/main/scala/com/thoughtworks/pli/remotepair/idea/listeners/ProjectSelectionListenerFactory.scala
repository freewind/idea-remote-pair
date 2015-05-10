package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{SelectionEvent, SelectionListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.{PublishEvent, InWatchingList}
import com.thoughtworks.pli.remotepair.idea.editor.GetSelectionEventInfo
import com.thoughtworks.pli.remotepair.idea.file.GetRelativePath

class ProjectSelectionListenerFactory(publishEvent: PublishEvent, logger: PluginLogger, inWatchingList: InWatchingList, getRelativePath: GetRelativePath, getSelectionEventInfo: GetSelectionEventInfo, isReadonlyMode: IsReadonlyMode)
  extends ListenerManager[SelectionListener] {

  val key = new Key[SelectionListener]("remote_pair.listeners.selection")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): SelectionListener = new SelectionListener {

    override def selectionChanged(e: SelectionEvent): Unit = if (inWatchingList(file) && !isReadonlyMode()) {
      logger.info("selectionChanged event: " + getSelectionEventInfo(e))
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

