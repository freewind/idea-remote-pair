//package com.thoughtworks.pli.remotepair.idea.listeners
//
//import com.intellij.openapi.editor.Editor
//import com.intellij.openapi.editor.event.CaretEvent
//import com.intellij.openapi.vfs.VirtualFile
//import com.thoughtworks.pli.intellij.remotepair.protocol.MoveCaretEvent
//import com.thoughtworks.pli.remotepair.idea.MocksModule
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Specification
//
//class ProjectCaretListenerFactorySpec extends Specification with Mockito with MocksModule {
//
//  isolated
//
//  override lazy val projectCaretListenerFactory = new ProjectCaretListenerFactory(publishEvent, pluginLogger, inWatchingList, getDocumentContent, getUserData, putUserData, getRelativePath, getCaretOffset, isReadonlyMode)
//
//  val KeyDocumentLength = projectCaretListenerFactory.KeyDocumentLength
//
//  val (file, editor, caretEvent) = (mock[VirtualFile], mock[Editor], mock[CaretEvent])
//
//  val listener = projectCaretListenerFactory.createNewListener(editor, file, currentProject)
//
//  inWatchingList.apply(file) returns true
//  getDocumentContent.apply(editor) returns "HelloWorld"
//  getUserData.apply(editor, KeyDocumentLength) returns Some(10)
//  getRelativePath(file) returns Some("/abc")
//  getCaretOffset(caretEvent) returns 3
//
//  "When caret position changes, ProjectCaretListener" should {
//    "publish event to server if the file is in the watching list" in {
//      inWatchingList.apply(file) returns true
//      listener.caretPositionChanged(caretEvent)
//      there was one(publishEvent).apply(MoveCaretEvent("/abc", 3))
//    }
//    "save current document length if it's the first caret event" in {
//      getUserData.apply(editor, KeyDocumentLength) returns None
//      listener.caretPositionChanged(caretEvent)
//      there was one(putUserData).apply(editor, KeyDocumentLength, 10)
//    }
//    "save current document length if it's the content length is changed" in {
//      getUserData.apply(editor, KeyDocumentLength) returns Some(99999999)
//      listener.caretPositionChanged(caretEvent)
//      there was one(putUserData).apply(editor, KeyDocumentLength, 10)
//    }
//    "not publish event to server if the file is not in the watching list" in {
//      inWatchingList.apply(file) returns false
//      listener.caretPositionChanged(caretEvent)
//      there was no(publishEvent).apply(MoveCaretEvent("/abc", 3))
//    }
//    "not publish event to server if current document length changed from last time" in {
//      getUserData.apply(editor, KeyDocumentLength) returns Some(99999999)
//      listener.caretPositionChanged(caretEvent)
//      there was no(publishEvent).apply(MoveCaretEvent("/abc", 3))
//    }
//    "not publish event to server if it's the first caret event" in {
//      getUserData.apply(editor, KeyDocumentLength) returns None
//      listener.caretPositionChanged(caretEvent)
//      there was no(publishEvent).apply(MoveCaretEvent("/abc", 3))
//    }
//  }
//}
