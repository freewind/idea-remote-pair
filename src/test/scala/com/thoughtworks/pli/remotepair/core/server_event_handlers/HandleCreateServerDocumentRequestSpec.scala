//package com.thoughtworks.pli.remotepair.core.server_event_handlers
//
//import com.thoughtworks.pli.remotepair.core.models.MyFile
//import com.thoughtworks.pli.intellij.remotepair.protocol.{CreateDocument, Content, CreateServerDocumentRequest}
//import com.thoughtworks.pli.remotepair.idea.MocksModule
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.document.HandleCreateServerDocumentRequest
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Specification
//
//class HandleCreateServerDocumentRequestSpec extends Specification with Mockito with MocksModule {
//  isolated
//
//  override lazy val handleCreateServerDocumentRequest = new HandleCreateServerDocumentRequest(runReadAction, publishEvent, getFileByRelative, getFileContent)
//
//  val file = mock[MyFile]
//
//  getFileByRelative.apply("/abc") returns Some(file)
//  getFileContent.apply(file) returns Content("hello world", "UTF-8")
//
//  val request = CreateServerDocumentRequest("/abc")
//
//  "When client receives CreateServerDocumentRequest, it" should {
//    "public CreateDocument event to server" in {
//      handleCreateServerDocumentRequest(request)
//      there was one(publishEvent).apply(CreateDocument("/abc", Content("hello world", "UTF-8")))
//    }
//    "not public CreateDocument event to server if can't find corresponding file" in {
//      getFileByRelative.apply("/abc") returns None
//      handleCreateServerDocumentRequest(request)
//      there was no(publishEvent).apply(CreateDocument("/abc", Content("hello world", "UTF-8")))
//    }
//  }
//
//}
