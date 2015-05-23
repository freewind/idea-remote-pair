package com.thoughtworks.pli.remotepair.core.server_event_handlers.login

import com.thoughtworks.pli.intellij.remotepair.protocol.ClientInfoResponse
import com.thoughtworks.pli.remotepair.core.client.MyClient

case class HandleClientInfoResponse(myClient: MyClient) {
  def apply(event: ClientInfoResponse) {
    myClient.clientInfoHolder.set(Some(event))
  }
}
