//package com.thoughtworks.pli.remotepair.core.server_event_handlers
//
//import com.thoughtworks.pli.intellij.remotepair.protocol.ClientInfoResponse
//import com.thoughtworks.pli.remotepair.idea.MocksModule
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.login.HandleClientInfoResponse
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Specification
//
//class HandleClientInfoResponseSpec extends Specification with Mockito with MocksModule {
//  isolated
//
//  override lazy val handleClientInfoResponse = new HandleClientInfoResponse(clientInfoHolder)
//
//  val event = mock[ClientInfoResponse]
//
//  "When client receives ClientInfoResponse, it" should {
//    "store it" in {
//      handleClientInfoResponse(event)
//      there was one(clientInfoHolder).put(Some(event))
//    }
//  }
//
//}
