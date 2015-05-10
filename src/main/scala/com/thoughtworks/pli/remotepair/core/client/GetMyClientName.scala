package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.remotepair.core.ClientInfoHolder

class GetMyClientName(clientInfoHolder: ClientInfoHolder) {

  def apply(): Option[String] = clientInfoHolder.get.map(_.name)

}
