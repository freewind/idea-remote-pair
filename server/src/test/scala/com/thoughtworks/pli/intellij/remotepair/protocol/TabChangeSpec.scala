package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair.MySpecification
import com.thoughtworks.pli.intellij.remotepair.ResetTabRequest
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class TabChangeSpec extends MySpecification {

  "OpenTabEvent" should {
    "be a lock when it sent" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test").shareCaret()

      client(context1).send(openTabEvent1)

      dataOf(context2).projectSpecifiedLocks.activeTabLocks.size === 1
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test").shareCaret()

      client(context1).send(openTabEvent1)
      client(context2).send(openTabEvent1)

      dataOf(context2).projectSpecifiedLocks.activeTabLocks.size === 0
      dataOf(context1).projectSpecifiedLocks.activeTabLocks.size === 0
    }
    "send ResetTabRequest to master if the feedback event is not matched" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test").shareCaret()

      client(context1).beMaster().send(openTabEvent1)
      client(context2).send(openTabEvent2)

      there was one(context1).writeAndFlush(ResetTabRequest.toMessage)
      there was no(context2).writeAndFlush(ResetTabRequest.toMessage)
    }
  }

  "TabResetEvent" should {
    "clear existing locks and be the new lock" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test").shareCaret()

      client(context1).send(openTabEvent1, resetTabEvent)

      val locks = dataOf(context2).projectSpecifiedLocks.activeTabLocks
      locks.size === 1
      locks.headOption === Some("/ccc")
    }
    "clear the master locks as well" in new ProtocolMocking {
      client(context1, context2)
      client(context1).beMaster()

      client(context2).send(openTabEvent1)
      client(context1).send(resetTabEvent)

      dataOf(context1).projectSpecifiedLocks.activeTabLocks.size === 0
    }
  }

  "CloseTabEvent" should {
    "broadcast to caret-sharing users" in new ProtocolMocking {
      client(context1, context2).createOrJoinProject("test").shareCaret()

      client(context1).send(closeTabEvent)

      there was one(context2).writeAndFlush(closeTabEvent.toMessage)
    }
  }

}
