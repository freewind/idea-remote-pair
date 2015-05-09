package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol.FileSummary

class GetWatchingFileSummaries(getAllWatchingFiles: GetAllWatchingFiles, getFileSummary: GetFileSummary) {
  def apply(): Seq[FileSummary] = getAllWatchingFiles().flatMap(getFileSummary.apply)
}
