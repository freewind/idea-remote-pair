package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol.FileSummary
import com.thoughtworks.pli.remotepair.idea.file.GetFileSummary

class GetWatchingFileSummaries(getAllWatchingFiles: GetAllWatchingFiles, getFileSummary: GetFileSummary) {
  def apply(): Seq[FileSummary] = getAllWatchingFiles().flatMap(getFileSummary.apply)
}
