package com.thoughtworks.pli.intellij.remotepair.protocol

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class CaretChangeSpec extends Specification with Mockito {

  "MoveCaretEvent" should {
    "be a lock when it sent" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(moveCaretEvent1)

      caretLock(context2, "/aaa").map(_.size) === Some(1)
    }
    "be locks for different files when they sent" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(moveCaretEvent1, moveCaretEvent3)

      caretLock(context2, "/aaa").map(_.size) === Some(1)
      caretLock(context2, "/bbb").map(_.size) === Some(1)
    }
    "clear the first lock if the feedback event is matched, and it won't be broadcasted" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(moveCaretEvent1)
      client(context2).send(moveCaretEvent1)

      caretLock(context2, "/aaa").map(_.size) === Some(0)
      caretLock(context1, "/aaa").map(_.size) === Some(0)
    }
    "send ResetCaretRequest to master if the feedback event is not matched" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).beMaster().send(moveCaretEvent1)
      client(context2).send(moveCaretEvent2)

      there was one(context1).writeAndFlush(resetCaretRequest1.toMessage)
      there was no(context2).writeAndFlush(resetCaretRequest1.toMessage)
    }
  }

  "ResetCaretEvent" should {
    "clear existing locks and be the new lock" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(moveCaretEvent1, resetCaretEvent1)

      caretLock(context2, "/aaa") must beSome.which { locks =>
        locks.size === 1
        locks.headOption === Some(15)
      }
    }
    "clear the master locks as well" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()
      client(context1).beMaster()

      client(context2).send(moveCaretEvent1)
      client(context1).send(resetCaretEvent1)

      caretLock(context1, "/aaa").map(_.size) === Some(0)
    }
  }

}
