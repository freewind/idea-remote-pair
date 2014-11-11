package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair.server.SelectionRange
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class ContentSelectionSpec extends Specification with Mockito {

  "SelectContentEvent" should {
    "be a lock when it sent" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(selectContentEvent1)

      selectionLock(context2, "/aaa").map(_.size) === Some(1)
    }
    "be locks for different files when they sent" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(selectContentEvent1, selectContentEvent3)

      selectionLock(context2, "/aaa").map(_.size) === Some(1)
      selectionLock(context2, "/bbb").map(_.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test")

      client(context1).send(selectContentEvent1)
      client(context2).send(selectContentEvent1)

      selectionLock(context2, "/aaa").map(_.size) === Some(0)
      selectionLock(context1, "/aaa").map(_.size) === Some(0)
    }
    "send ResetSelectionRequest to master if the feedback event is not matched" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).beMaster().send(selectContentEvent1)
      client(context2).send(selectContentEvent2)

      there was one(context1).writeAndFlush(resetSelectionRequest.toMessage)
      there was no(context2).writeAndFlush(resetSelectionRequest.toMessage)
    }
  }

  "ResetSelectionEvent" should {
    "clear existing locks and be the new lock" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(selectContentEvent1, resetSelectionEvent)

      selectionLock(context2, "/aaa") must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some(SelectionRange(30, 12))
      }
    }
    "clear the master locks as well" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).beMaster()
      client(context2).send(selectContentEvent1)
      client(context1).send(resetSelectionEvent)

      selectionLock(context1, "/aaa").map(_.size) === Some(0)
    }
  }

}
