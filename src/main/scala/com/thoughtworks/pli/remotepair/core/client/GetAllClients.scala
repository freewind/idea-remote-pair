package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol.ClientInfoResponse

class GetAllClients(getProjectInfoData: GetProjectInfoData) {
  def apply(): Seq[ClientInfoResponse] = getProjectInfoData().toSeq.flatMap(_.clients).toSeq
}
