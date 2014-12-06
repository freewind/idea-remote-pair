package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair.MySpecification

class ContentChangeSpec extends MySpecification {

  "ResetContentEvent" should {
    "clear all content locks of a specified file path and be a new lock" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(changeContentEventA1, changeContentEventA2, resetContentEvent)

      dataOf(context2).pathSpecifiedLocks.get("/aaa").map(_.contentLocks) must beSome.which { locks =>
        locks.size === 1
        locks.headOption.get === "s4"
      }
    }
    "clear the master content locks as well" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context2).send(changeContentEventA1)
      client(context1).send(resetContentEvent)

      dataOf(context1).pathSpecifiedLocks.get("/aaa") must beSome.which(_.contentLocks.size === 0)
    }
  }

}

