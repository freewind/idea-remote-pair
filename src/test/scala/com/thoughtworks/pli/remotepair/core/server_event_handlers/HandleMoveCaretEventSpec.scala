package com.thoughtworks.pli.remotepair.core.server_event_handlers

import com.intellij.openapi.editor.ex.EditorEx
import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
import com.thoughtworks.pli.remotepair.idea.MocksModule
import com.thoughtworks.pli.remotepair.core.server_event_handlers.editors.HandleMoveCaretEvent
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class HandleMoveCaretEventSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val handleMoveCaretEvent = new HandleMoveCaretEvent(invokeLater, getTextEditorsOfPath, isCaretSharing, scrollToCaretInEditor, convertEditorOffsetToPoint, drawCaretInEditor)

  val editor = mock[EditorEx]
  val event = new MoveCaretEvent("/abc", 123)
  getTextEditorsOfPath.apply("/abc") returns Seq(editor)
  isCaretSharing.apply() returns true

  "When clients receives MoveCaretEvent, it" should {
    "draw a caret icon in corresponding position" in {
      handleMoveCaretEvent(event)
      there was one(drawCaretInEditor).apply(editor, 123)
    }
    "scroll to the caret position if in shareCaret mode" in {
      handleMoveCaretEvent(event)
      there was one(scrollToCaretInEditor).apply(editor, 123)
    }
    "not scroll if not in shareCaret mode" in {
      isCaretSharing.apply() returns false
      handleMoveCaretEvent(event)
      there was no(scrollToCaretInEditor).apply(any, any)
    }
    "do anything if no editors found for specified path" in {
      getTextEditorsOfPath.apply("/abc") returns Nil
      handleMoveCaretEvent(event)
      there was no(drawCaretInEditor).apply(any, any)
    }
    "ignore errors when drawing caret, and won't propagate" in {
      drawCaretInEditor.apply(any, any) throws new ArrayIndexOutOfBoundsException
      handleMoveCaretEvent(event) must not be throwA[Throwable]
    }
  }

}
