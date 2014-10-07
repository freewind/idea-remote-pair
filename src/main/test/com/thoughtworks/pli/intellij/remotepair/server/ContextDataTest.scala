package com.thoughtworks.pli.intellij.remotepair.server

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import io.netty.channel.ChannelHandlerContext

class ContextDataTest extends Specification with Mockito {

  "ContextData" should {
    "add ChangeContentEvent for different files" in new Mocking {
      contextData.contentLocks.add("/aaa", "s1")
      contextData.contentLocks.add("/bbb", "s2")

      contextData.contentLocks.size === 2
    }
  }

  trait Mocking extends Scope {
    val context = mock[ChannelHandlerContext]
    val contextData = new ContextData(context)
  }

}
