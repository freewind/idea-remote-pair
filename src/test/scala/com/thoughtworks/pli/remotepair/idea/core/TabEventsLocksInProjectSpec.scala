package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class TabEventsLocksInProjectSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val tabEventsLocksInProject = new TabEventsLocksInProject(currentProjectScope, getCurrentTimeMillis)

  private val events = mock[ValueInCurrentProject[Seq[TabEventLock]]]
  currentProjectScope.value[Seq[TabEventLock]](any, any) returns events

  private var _savedEvents: Seq[TabEventLock] = Nil
  events.get answers { _ => _savedEvents}
  events.set(any) answers { v => _savedEvents = v.asInstanceOf[Seq[TabEventLock]]; _savedEvents}

  private val lock1 = new TabEventLock("/aaa", 123000L)
  private val lock2 = new TabEventLock("/bbb", 123456L)
  private val lock3 = new TabEventLock("/ccc", 123456L + 2001L)

  "When add a new lock, it" should {
    "add it if everything is well" in {
      tabEventsLocksInProject.lock(lock1)
      tabEventsLocksInProject.lock(lock2)
      there was one(events).set(Seq(lock1, lock2))
    }
    "clear all old locks if it's later than previous one more than 2s" in {
      tabEventsLocksInProject.lock(lock2)
      tabEventsLocksInProject.lock(lock3)
      there was one(events).set(Seq(lock3))
    }
  }

  "When unlock a lock, it" should {
    "return true and remove the oldest one if its path matches the passing lock" in {
      tabEventsLocksInProject.lock(lock1)
      tabEventsLocksInProject.lock(lock2)
      val result = tabEventsLocksInProject.unlock("/aaa")
      result === true
      events.get === Seq(lock2)
    }
    "return false and remove nothing if the path of oldest exsting lock is not matches the passing lock" in {
      tabEventsLocksInProject.lock(lock1)
      tabEventsLocksInProject.lock(lock2)
      val result = tabEventsLocksInProject.unlock("/bbb")
      result === false
      events.get === Seq(lock1, lock2)
    }
    "clear all old locks if it's later than any of previous ones more than 2s" in {
      tabEventsLocksInProject.lock(lock2)
      getCurrentTimeMillis.apply() returns (123456L + 2001L)
      val result = tabEventsLocksInProject.unlock("/any")
      result === false
      events.get === Nil
    }
  }

}
