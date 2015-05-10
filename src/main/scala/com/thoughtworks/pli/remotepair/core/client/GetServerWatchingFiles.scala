package com.thoughtworks.pli.remotepair.core.client

class GetServerWatchingFiles(getProjectInfoData: GetProjectInfoData) {

  def apply(): Seq[String] = getProjectInfoData().map(_.watchingFiles).getOrElse(Nil)

}
