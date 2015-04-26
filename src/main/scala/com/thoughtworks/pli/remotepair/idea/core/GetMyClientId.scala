package com.thoughtworks.pli.remotepair.idea.core

class GetMyClientId(clientInfoHolder: ClientInfoHolder) {

  def apply(): Option[String] = clientInfoHolder.get.map(_.clientId)

}

class GetMyClientName(clientInfoHolder: ClientInfoHolder) {

  def apply(): Option[String] = clientInfoHolder.get.map(_.name)

}

