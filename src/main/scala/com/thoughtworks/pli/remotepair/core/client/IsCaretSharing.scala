package com.thoughtworks.pli.remotepair.core.client

case class IsCaretSharing(getProjectInfoData: GetProjectInfoData) {
  def apply(): Boolean = getProjectInfoData().exists(_.isCaretSharing)
}
