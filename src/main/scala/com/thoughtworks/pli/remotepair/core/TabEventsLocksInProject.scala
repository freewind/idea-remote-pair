package com.thoughtworks.pli.remotepair.core

import com.intellij.openapi.util.Key

object TabEventsLocksInProject {
  val Key = new Key[Seq[TabEventLock]](TabEventsLocksInProject.getClass.getName)
}

class TabEventsLocksInProject(currentProjectScope: CurrentProjectScope, getCurrentTimeMillis: GetCurrentTimeMillis) {
  private val events = currentProjectScope.value(TabEventsLocksInProject.Key, Nil)
  def lock(lock: TabEventLock) = {
    events.get match {
      case es if needClearOldLocks(es, lock.timestamp) => events.set(Seq(lock))
      case _ => events.set(events.get :+ lock)
    }
  }
  def unlock(path: String): Boolean = {
    events.get match {
      case es if needClearOldLocks(es, getCurrentTimeMillis()) => events.set(Nil)
        false
      case Seq(head, tail@_*) if head.path == path =>
        events.set(tail)
        true
      case _ => false
    }
  }
  def isEmpty = events.get.size == 0

  private def needClearOldLocks(existingLocks: Seq[TabEventLock], newTimestamp: Long): Boolean =
    existingLocks.lastOption.exists(tail => newTimestamp - tail.timestamp > 2000)
}

case class TabEventLock(path: String, timestamp: Long)
