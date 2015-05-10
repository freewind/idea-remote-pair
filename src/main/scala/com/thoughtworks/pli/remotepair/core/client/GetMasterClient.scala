package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol.ClientInfoResponse

class GetMasterClient(getProjectInfoData: GetProjectInfoData) {
  def apply(): Option[ClientInfoResponse] = getProjectInfoData().flatMap(_.clients.find(_.isMaster))
}
