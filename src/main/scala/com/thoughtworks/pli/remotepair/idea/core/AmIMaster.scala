package com.thoughtworks.pli.remotepair.idea.core

class AmIMaster(clientInfoHolder: ClientInfoHolder) {
  def apply(): Boolean = clientInfoHolder.get.exists(_.isMaster)
}
