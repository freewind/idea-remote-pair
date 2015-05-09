package com.thoughtworks.pli.remotepair.idea.core

class GetMyClientName(clientInfoHolder: ClientInfoHolder) {

  def apply(): Option[String] = clientInfoHolder.get.map(_.name)

}
