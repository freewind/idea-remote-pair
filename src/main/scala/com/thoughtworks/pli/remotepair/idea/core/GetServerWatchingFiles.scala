package com.thoughtworks.pli.remotepair.idea.core

class GetServerWatchingFiles(getProjectInfoData: GetProjectInfoData) {

  def apply(): Seq[String] = getProjectInfoData().map(_.watchingFiles).getOrElse(Nil)

}
