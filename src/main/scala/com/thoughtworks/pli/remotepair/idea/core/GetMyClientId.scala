package com.thoughtworks.pli.remotepair.idea.core

class GetMyClientId(clientInfo: ClientInfoHolder) {

  def apply(): Option[String] = clientInfo.get.map(_.clientId)

}
