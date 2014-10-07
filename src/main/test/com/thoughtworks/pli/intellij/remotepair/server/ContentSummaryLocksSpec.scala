package com.thoughtworks.pli.intellij.remotepair.server

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ContentSummaryLocksSpec extends Specification {

  "ContentSummaryLocks" should {
    "add multiple locks for a file" in new Mocking {
      locks.add("/aaa", "s1")
      locks.add("/aaa", "s2")
      locks.size === 1
      locks.get("/aaa") must beSome.which(_.size === 2)
    }
    "add locks for multiple files" in new Mocking {
      locks.add("/aaa", "s1")
      locks.add("/bbb", "s3")
      locks.size === 2
    }
  }

  trait Mocking extends Scope {
    val locks = new ContentSummaryLocks
  }

}
