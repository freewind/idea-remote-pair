package com.thoughtworks.pli.intellij.remotepair.server

import com.thoughtworks.pli.intellij.remotepair.{MyMocking, MySpecification}

class PathSpecifiedLocksSpec extends MySpecification {

  "PathSpecifiedLocksSpec" should {
    "get existing PathLocks for specified path" in new Mocking {
      locks.getOrCreate("/aaa") // will create one
      locks.get("/aaa") must beSome.which(_ must haveClass[PathLocks])
    }
    "get None if speicified path is not exist" in new Mocking {
      locks.get("/non-exist") === None
    }
    "create new one if specified path is not-exist" in new Mocking {
      locks.getOrCreate("/aaa") // will create one
      locks.size === 1
    }
  }

  "ContentSummaryLocks" should {
    "add multiple locks for a file" in new Mocking {
      locks.getOrCreate("/aaa").contentLocks.add("s1")
      locks.getOrCreate("/aaa").contentLocks.add("s2")
      locks.size === 1
      locks.get("/aaa") must beSome.which(_.contentLocks.size === 2)
    }
    "add locks for multiple files" in new Mocking {
      locks.getOrCreate("/aaa").contentLocks.add("s1")
      locks.getOrCreate("/bbb").contentLocks.add("s3")
      locks.size === 2
    }
  }

  trait Mocking extends MyMocking {
    val locks = new PathSpecifiedLocks
  }

}
