package com.thoughtworks.pli.remotepair.in_memory_ide.models

import org.specs2.mutable.Specification

class InMemoryFilePathSpec extends Specification {

  "InMemoryFilePathSpec" should {
    val path = new InMemoryFilePath("/aaa/bbb/ccc")
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
      path.rename("ddd") === InMemoryFilePath("/aaa/bbb/ddd")
    }
    "create a child file" in {
      path.child("ddd") === InMemoryFilePath("/aaa/bbb/ccc/ddd")
    }
    "get parent" in {
      path.parent === InMemoryFilePath("/aaa/bbb")
    }
    "relativeTo to an existing parent" in {
      path.relativeTo(InMemoryFilePath("/aaa")) ==== Some("/bbb/ccc")
    }
    "relativeTo to an non-existing parent" in {
      path.relativeTo(InMemoryFilePath("/non-existing")) ==== None
    }
  }

  "root path" should {
    "have no items" in {
      InMemoryFilePath("/").items === Nil
    }
  }
}
