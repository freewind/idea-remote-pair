package com.thoughtworks.pli.remotepair.core.client

case class ClientIdToName(getProjectInfoData: GetProjectInfoData) {
  def apply(clientId: String): Option[String] = {
    getProjectInfoData().flatMap(_.clients.find(_.clientId == clientId)).map(_.name)
  }
}
