package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.remotepair.core.ClientInfoHolder

class GetMyClientId(clientInfoHolder: ClientInfoHolder) {

  def apply(): Option[String] = clientInfoHolder.get.map(_.clientId)

}



