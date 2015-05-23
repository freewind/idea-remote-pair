package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.remotepair.core.models.{DataKey, MyProject}
import com.thoughtworks.pli.remotepair.core.{MySystem, ProjectScopeValue}

class TabEventsLocksInProject(myProject: MyProject, mySystem: MySystem) {
  private val events = new ProjectScopeValue(myProject, new DataKey[Seq[TabEventLock]](this.getClass.getName), Nil)
  def lock(lock: TabEventLock) = {
    events.get match {
      case es if needClearOldLocks(es, lock.timestamp) => events.set(Seq(lock))
      case _ => events.set(events.get :+ lock)
    }
  }
  def unlock(path: String): Boolean = {
    events.get match {
      case es if needClearOldLocks(es, mySystem.now) => events.set(Nil)
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
