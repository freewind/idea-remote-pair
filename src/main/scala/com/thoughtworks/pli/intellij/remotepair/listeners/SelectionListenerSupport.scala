package com.thoughtworks.pli.intellij.remotepair.listeners

import com.intellij.openapi.editor.event.{SelectionEvent, SelectionListener}
import com.intellij.openapi.util.{TextRange, Key}
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.{ClientContextHolder, RelativePathResolver, SelectContentEvent, PublishEvents}

trait SelectionListenerSupport extends RelativePathResolver with PublishEvents {
  this: ClientContextHolder =>

  def createSelectionListener(): ListenerManageSupport[SelectionListener] = new ListenerManageSupport[SelectionListener] {
    val key = new Key[SelectionListener]("remote_pair.listeners.selection")

    def createNewListener(editor: Editor, file: VirtualFile, project: Project): SelectionListener = new SelectionListener {

      override def selectionChanged(e: SelectionEvent): Unit = {
        val path = mypath(file.getPath, project)
        val range = e.getNewRange
        val event = SelectContentEvent(path, range.getStartOffset, range.getEndOffset - range.getStartOffset)
        publishEvent(event)
        println("####### selectionChanged: " + selectionEventInfo(e))
      }

      private def selectionEventInfo(e: SelectionEvent) = s"${e.getOldRanges.toList.map(textRangeInfo)} => ${e.getNewRanges.toList.map(textRangeInfo)}"

      private def textRangeInfo(textRange: TextRange) = s"${textRange.getStartOffset} -> ${textRange.getEndOffset} (${textRange.getLength}})"
    }

    override def originAddListener(editor: Editor): (SelectionListener) => Any = editor.getSelectionModel.addSelectionListener

    override def originRemoveListener(editor: Editor): (SelectionListener) => Any = editor.getSelectionModel.removeSelectionListener

  }


}
