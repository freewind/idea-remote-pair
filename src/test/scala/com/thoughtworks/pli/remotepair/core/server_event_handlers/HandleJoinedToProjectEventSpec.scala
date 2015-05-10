//package com.thoughtworks.pli.remotepair.core.server_event_handlers
//
//import com.thoughtworks.pli.remotepair.core.models.MyFile
//import com.thoughtworks.pli.intellij.remotepair.protocol.JoinedToProjectEvent
//import com.thoughtworks.pli.remotepair.idea.MocksModule
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.login.HandleJoinedToProjectEvent
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Specification
//
//class HandleJoinedToProjectEventSpec extends Specification with Mockito with MocksModule {
//  isolated
//
//  override lazy val handleJoinedToProjectEvent = new HandleJoinedToProjectEvent(getOpenedFiles, runWriteAction, closeFile)
//
//  val file1 = mock[MyFile]
//  val file2 = mock[MyFile]
//
//  getOpenedFiles.apply() returns Seq(file1, file2)
//
//  val event = mock[JoinedToProjectEvent]
//
//  "When client receives JoinedToProjectEvent, it" should {
//    "publish CreateDocumentEvent for all open files" in {
//      handleJoinedToProjectEvent(event)
//      there was one(publishCreateDocumentEvent).apply(file1)
//      there was one(publishCreateDocumentEvent).apply(file2)
//    }
//    "not publish CreateDocumentEvent if no open files" in {
//      getOpenedFiles.apply() returns Nil
//      handleJoinedToProjectEvent(event)
//      there was no(publishCreateDocumentEvent).apply(any)
//    }
//  }
//
//}
