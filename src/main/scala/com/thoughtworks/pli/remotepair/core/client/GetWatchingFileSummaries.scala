package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol.FileSummary

class GetWatchingFileSummaries(getAllWatchingFiles: GetAllWatchingFiles) {
  def apply(): Seq[FileSummary] = getAllWatchingFiles().flatMap(_.summary)
}
