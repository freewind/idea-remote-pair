package com.thoughtworks.pli.remotepair.idea.core

case class IsCaretSharing(getProjectInfoData: GetProjectInfoData) {
  def apply(): Boolean = getProjectInfoData().exists(_.isCaretSharing)
}
