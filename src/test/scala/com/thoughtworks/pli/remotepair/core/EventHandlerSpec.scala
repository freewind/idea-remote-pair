//package com.thoughtworks.pli.remotepair.idea.core
//
//import com.intellij.openapi.editor.Editor
//import com.thoughtworks.pli.intellij.remotepair.protocol._
//import com.thoughtworks.pli.remotepair.idea.dialogs.{JoinProjectDialog, MockCurrentProjectHolder}
//import com.thoughtworks.pli.remotepair.idea.{MyMocking, MySpecification}
//
//class EventHandlerSpec extends MySpecification {
//
//  "ClientInfoResponse" should {
//    "be kept" in new Mocking {
//      handler.handleEvent(clientInfoResponse)
//      there was one(handler.currentProject).clientInfo_=(Some(clientInfoResponse))
//    }
//  }
//
//  trait Mocking extends MyMocking with MockCurrentProjectHolder {
//    m =>
//
//    val invokeLater = new MockInvokeLater
//
//    val dialogCreated = mock[Any => Any]
//
//    val publishEvent = mock[PairEvent => Any]
//
//    class MyHandleEvent extends HandleEvent with MockCurrentProject {
//      override def createJoinProjectDialog(address: ServerAddress) = {
//        dialogCreated("JoinProjectDialog")
//        mock[JoinProjectDialog]
//      }
//      override def invokeLater(f: => Any) = m.invokeLater(f)
//      override def apply(event: PairEvent) = m.publishEvent(event)
//      override def runReadAction(f: => Any) = f
//      override def runWriteAction(f: => Any) = f
//      override def md5(s: String): String = s + "-md5"
//    }
//
//    val clientInfoResponse = ClientInfoResponse("cid", "test", "Freewind", isMaster = true)
//
//    val textEditor = deepMock[Editor]
//
//    def mockEditorContent(path: String, content: String) {
//      textEditor.getDocument.getText returns content
//      currentProject.getTextEditorsOfPath(path) returns Seq(textEditor)
//    }
//
//    val handler = new MyHandleEvent
//  }
//
//}
