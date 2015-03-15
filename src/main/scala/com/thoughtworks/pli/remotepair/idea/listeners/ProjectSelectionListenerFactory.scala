package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{SelectionEvent, SelectionListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.{Key, TextRange}
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core._

case class ProjectSelectionListenerFactory(currentProject: RichProject, publishEvent: PublishEvent, logger: Logger, inWatchingList: InWatchingList)
  extends ListenerManager[SelectionListener] {

  val key = new Key[SelectionListener]("remote_pair.listeners.selection")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): SelectionListener = new SelectionListener {
    private def ifInWatching(f: => Any): Unit = if (inWatchingList(file)) f

    override def selectionChanged(e: SelectionEvent): Unit = ifInWatching {
      for {
        path <- currentProject.getRelativePath(file)
        range = e.getNewRange
        event = SelectContentEvent(path, range.getStartOffset, range.getEndOffset - range.getStartOffset)
      } {
        publishEvent(event)
        logger.info("####### selectionChanged: " + selectionEventInfo(e))
      }
    }

    private def selectionEventInfo(e: SelectionEvent) = s"${e.getOldRanges.toList.map(textRangeInfo)} => ${e.getNewRanges.toList.map(textRangeInfo)}"

    private def textRangeInfo(textRange: TextRange) = s"${textRange.getStartOffset} -> ${textRange.getEndOffset} (${textRange.getLength}})"
  }

  override def originAddListener(editor: Editor): (SelectionListener) => Any = editor.getSelectionModel.addSelectionListener

  override def originRemoveListener(editor: Editor): (SelectionListener) => Any = editor.getSelectionModel.removeSelectionListener

}

