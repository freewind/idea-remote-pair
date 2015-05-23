package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{SelectionEvent, SelectionListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.{Key, TextRange}
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.editor_event_handlers.{EditorSelectionChangeEvent, HandleIdeaEvent}
import com.thoughtworks.pli.remotepair.idea.models.IdeaFactories

class ProjectSelectionListenerFactory(logger: PluginLogger, handleIdeaEvent: HandleIdeaEvent, ideaFactories: IdeaFactories)
  extends ListenerManager[SelectionListener] {

  val key = new Key[SelectionListener]("remote_pair.listeners.selection")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): SelectionListener = new SelectionListener {
    override def selectionChanged(e: SelectionEvent): Unit = {
      logger.info("selectionChanged event: " + getSelectionEventInfo(e))
      val range = e.getNewRange
      handleIdeaEvent(new EditorSelectionChangeEvent(ideaFactories(file), ideaFactories(editor), range.getStartOffset, range.getEndOffset - range.getStartOffset))
    }
  }

  override def originAddListener(editor: Editor): (SelectionListener) => Any = editor.getSelectionModel.addSelectionListener

  override def originRemoveListener(editor: Editor): (SelectionListener) => Any = editor.getSelectionModel.removeSelectionListener

  def getSelectionEventInfo(event: SelectionEvent): String = {
    def textRangeInfo(textRange: TextRange): String = s"${textRange.getStartOffset} -> ${textRange.getEndOffset} (${textRange.getLength}})"
    s"${event.getOldRanges.toList.map(textRangeInfo)} => ${event.getNewRanges.toList.map(textRangeInfo)}"
  }

}
