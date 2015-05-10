package com.thoughtworks.pli.remotepair.core.client

class GetMasterClientId(getProjectInfoData: GetProjectInfoData) {
  def apply(): Option[String] = getProjectInfoData().flatMap(_.clients.find(_.isMaster)).map(_.clientId)
}

