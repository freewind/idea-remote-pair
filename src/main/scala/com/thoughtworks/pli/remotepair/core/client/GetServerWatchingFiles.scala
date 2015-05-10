package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.remotepair.core.client.GetProjectInfoData

class GetServerWatchingFiles(getProjectInfoData: GetProjectInfoData) {

  def apply(): Seq[String] = getProjectInfoData().map(_.watchingFiles).getOrElse(Nil)

}
