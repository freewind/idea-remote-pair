package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{SelectionEvent, SelectionListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.idea_event_handlers.{HandleIdeaEvent, IdeaSelectionChangeEvent}
import com.thoughtworks.pli.remotepair.idea.editor.GetSelectionEventInfo
import com.thoughtworks.pli.remotepair.idea.models.IdeaFileImpl

class ProjectSelectionListenerFactory(logger: PluginLogger, handleIdeaEvent: HandleIdeaEvent, getSelectionEventInfo: GetSelectionEventInfo)
  extends ListenerManager[SelectionListener] {

  val key = new Key[SelectionListener]("remote_pair.listeners.selection")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): SelectionListener = new SelectionListener {
    override def selectionChanged(e: SelectionEvent): Unit = {
      logger.info("selectionChanged event: " + getSelectionEventInfo(e))
      val range = e.getNewRange
      handleIdeaEvent(new IdeaSelectionChangeEvent(IdeaFileImpl(file), editor, range.getStartOffset, range.getEndOffset - range.getStartOffset))
    }
  }

  override def originAddListener(editor: Editor): (SelectionListener) => Any = editor.getSelectionModel.addSelectionListener

  override def originRemoveListener(editor: Editor): (SelectionListener) => Any = editor.getSelectionModel.removeSelectionListener

}
