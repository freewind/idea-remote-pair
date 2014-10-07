package com.thoughtworks.pli.intellij.remotepair.server

import scala.collection.concurrent.TrieMap

class ContentSummaryLocks {

  private val locks = new TrieMap[String, EventLocks[String]]

  def add(path: String, summary: String) {
    val summaryLocks = locks.get(path) match {
      case Some(l) => l
      case _ =>
        val eventLocks = new EventLocks[String]()
        locks.put(path, eventLocks)
        eventLocks
    }
    summaryLocks.add(summary)
  }

  def size = locks.size

  def get(path: String): Option[EventLocks[String]] = locks.get(path)

}
