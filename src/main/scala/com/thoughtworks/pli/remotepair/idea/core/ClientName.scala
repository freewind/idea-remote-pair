package com.thoughtworks.pli.remotepair.idea.core

case class ClientName(getProjectInfoData: GetProjectInfoData) {
  def unapply(clientId: String): Option[String] = {
    getProjectInfoData().flatMap(_.clients.find(_.clientId == clientId)).map(_.name)
  }
}
