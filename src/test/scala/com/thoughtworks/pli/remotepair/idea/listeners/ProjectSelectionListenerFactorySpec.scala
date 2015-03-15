package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class ProjectSelectionListenerFactorySpec extends Specification with Mockito with MocksModule {

  isolated

  override lazy val projectSelectionListenerFactory: ProjectSelectionListenerFactory = wire[ProjectSelectionListenerFactory]

  val (editor, file, event) = (mock[Editor], mock[VirtualFile], mock[SelectionEvent])
  val listener = projectSelectionListenerFactory.createNewListener(editor, file, currentProject)

  val newRange = new TextRange(3, 7)

  inWatchingList.apply(file) returns true
  getRelativePath.apply(file) returns Some("/abc")
  event.getNewRange returns newRange

  "ProjectSelectionListener" should {
    "publish SelectContentEvent to server" in {
      listener.selectionChanged(event)
      there was one(publishEvent).apply(SelectContentEvent("/abc", 3, 4))
    }
    "not publish SelectContentEvent if the file is not in watching list" in {
      inWatchingList.apply(file) returns false
      listener.selectionChanged(event)
      there was no(publishEvent).apply(any[SelectContentEvent])
    }
  }
}
