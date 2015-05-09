package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol.ClientInfoResponse

class GetMasterClient(getProjectInfoData: GetProjectInfoData) {
  def apply(): Option[ClientInfoResponse] = getProjectInfoData().flatMap(_.clients.find(_.isMaster))
}
