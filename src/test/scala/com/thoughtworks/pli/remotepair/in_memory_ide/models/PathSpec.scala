package com.thoughtworks.pli.remotepair.in_memory_ide.models

import org.specs2.mutable.Specification

class PathSpec extends Specification {

  "Path" should {
    val path = new Path("aaa/bbb/ccc")
    "have correct filename" in {
      path.filename === "ccc"
    }
    "not be root" in {
      path.isRoot === false
    }
    "have 3 path items" in {
      path.items === Seq("aaa", "bbb", "ccc")
    }
    "rename the filename" in {
      path.rename("ddd") === Path("aaa/bbb/ddd")
    }
    "create a child file" in {
      path.child("ddd") === Path("aaa/bbb/ccc/ddd")
    }
    "get parent" in {
      path.parent === Path("aaa/bbb")
    }
    "relativeTo to an existing parent" in {
      path.relativeTo(Path("aaa")) ==== Some("bbb/ccc")
    }
    "relativeTo to an non-existing parent" in {
      path.relativeTo(Path("non-existing")) ==== None
    }
  }
}
