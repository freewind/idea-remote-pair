//package com.thoughtworks.pli.remotepair.core.server_event_handlers
//
//import com.thoughtworks.pli.intellij.remotepair.protocol.ServerStatusResponse
//import com.thoughtworks.pli.remotepair.idea.MocksModule
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.login.HandleServerStatusResponse
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Specification
//
//class HandleServerStatusResponseSpec extends Specification with Mockito with MocksModule {
//  isolated
//
//  override lazy val handleServerStatusResponse = new HandleServerStatusResponse(serverStatusHolder)
//
//  val response = mock[ServerStatusResponse]
//
//  "When client receives ServerStatusResponse, it" should {
//    "put it into serverStatusHolder" in {
//      handleServerStatusResponse(response)
//      there was one(serverStatusHolder).put(Some(response))
//    }
//  }
//
//}
