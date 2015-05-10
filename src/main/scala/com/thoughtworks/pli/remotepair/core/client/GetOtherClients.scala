package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol.ClientInfoResponse

class GetOtherClients(getAllClients: GetAllClients, getMyClientId: GetMyClientId) {
  def apply(): Seq[ClientInfoResponse] = getAllClients().filterNot(client => Some(client.clientId) == getMyClientId())
}
