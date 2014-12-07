package com.thoughtworks.pli.intellij.remotepair.server

import com.thoughtworks.pli.intellij.remotepair.{MyMocking, MySpecification}
import io.netty.channel.ChannelHandlerContext

class ClientSpec extends MySpecification {

  "ContextData" should {
    "add ChangeContentEvent for different files" in new Mocking {
      contextData.pathSpecifiedLocks.getOrCreate("/aaa").contentLocks.add("s1")
      contextData.pathSpecifiedLocks.getOrCreate("/bbb").contentLocks.add("s2")

      contextData.pathSpecifiedLocks.size === 2
    }
  }

  trait Mocking extends MyMocking {
    val context = mock[ChannelHandlerContext]
    val contextData = new Client(context)
  }

}
