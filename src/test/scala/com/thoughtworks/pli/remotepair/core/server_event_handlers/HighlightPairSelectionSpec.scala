//package com.thoughtworks.pli.remotepair.core.server_event_handlers
//
//import java.awt.Color
//
//import com.intellij.openapi.editor.Editor
//import com.intellij.openapi.editor.markup.TextAttributes
//import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
//import com.thoughtworks.pli.remotepair.idea.MocksModule
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.editors.HighlightPairSelection
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Specification
//
//class HighlightPairSelectionSpec extends Specification with Mockito with MocksModule {
//  isolated
//
//  override lazy val highlightPairSelection = new HighlightPairSelection(getTextEditorsOfPath, invokeLater, publishEvent, newHighlights, removeOldHighlighters, pluginLogger, getDocumentLength)
//
//  val editor = mock[Editor]
//  val event = SelectContentEvent("/abc", 20, 7)
//  getTextEditorsOfPath.apply("/abc") returns Seq(editor)
//
//  val textAttr = new TextAttributes(null, Color.GREEN, null, null, 0)
//
//  "When client receives SelectContentEvent, it" should {
//    "highlight the corresponding text in the editors of same path" in {
//      highlightPairSelection(event)
//      there was one(newHighlights).apply(HighlightPairSelection.SelectionHighlightKey, editor, textAttr, Seq(Range(20, 27)))
//    }
//    "remove old highlights" in {
//      highlightPairSelection(event)
//      there was one(removeOldHighlighters).apply(HighlightPairSelection.SelectionHighlightKey, editor)
//    }
//    "not highlight anything if length of selection is 0" in {
//      val event = SelectContentEvent("/abc", 20, 0)
//      highlightPairSelection(event)
//      there was no(newHighlights).apply(any, any, any, any)
//    }
//    "not throw exception and go on highlighting if error happens when removing old highlights" in {
//      removeOldHighlighters.apply(any, any) throws new ArrayIndexOutOfBoundsException("some-exception")
//      highlightPairSelection(event)
//      there was one(newHighlights).apply(HighlightPairSelection.SelectionHighlightKey, editor, textAttr, Seq(Range(20, 27)))
//    }
//    "not throw exception and highlight closest range if error happens when highlighting" in {
//      getDocumentLength.apply(editor) returns 25
//      newHighlights.apply(any, any, any, ===(Seq(Range(20, 27)))) throws new ArrayIndexOutOfBoundsException("some-exception")
//      highlightPairSelection(event)
//      there was one(newHighlights).apply(HighlightPairSelection.SelectionHighlightKey, editor, textAttr, Seq(Range(20, 25)))
//    }
//
//  }
//}
