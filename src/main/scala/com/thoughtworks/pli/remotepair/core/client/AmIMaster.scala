package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.remotepair.core.ClientInfoHolder

class AmIMaster(clientInfoHolder: ClientInfoHolder) {
  def apply(): Boolean = clientInfoHolder.get.exists(_.isMaster)
}
