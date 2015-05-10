package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.intellij.remotepair.utils.ContentDiff

case class Change(eventId: String, baseVersion: Int, diffs: Seq[ContentDiff])
