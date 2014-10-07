package com.thoughtworks.pli.intellij.remotepair.server

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class EventLocksTest extends Specification {

  "An event" should {

    "be added as a lock" in new Mocking {
      locks.add("aaa")
      locks.add("bbb")
      locks.size === 2
    }
  }

  "Locks" should {
    "remove the first lock if there is" in new Mocking {
      locks.add("aaa")
      locks.add("bbb")

      locks.removeHead()
      locks.size === 1
    }

    "remove nothing if it is empty" in new Mocking {
      locks.removeHead()
      locks.size === 0
    }

    "be all cleared" in new Mocking {
      locks.add("aaa")
      locks.clear()
      locks.size === 0
    }

    "return the head" in new Mocking {
      locks.add("aaa")
      locks.add("bbb")

      locks.headOption === Some("aaa")
    }

  }

  trait Mocking extends Scope {
    val locks = new EventLocks[String]
  }

}
