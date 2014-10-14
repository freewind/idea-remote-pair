package com.thoughtworks.pli.intellij.remotepair.server

import org.specs2.mutable.Specification
import io.netty.channel.ChannelHandlerContext
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import scala.collection.mutable

class ContextHolderSpec extends Specification with Mockito {

  "ContextHolder" should {
    "add new context" in new Mocking {
      val created = holder.add(context)
      holder.size === 1
      created.context === context
    }

    "remove context" in new Mocking {
      holder.add(context)
      holder.remove(context)
      holder.size === 0
    }

    "get existing events object for an cached context" in new Mocking {
      holder.add(context)
      holder.get(context) === Some(rich)
    }

    "get None if the context is not exist" in new Mocking {
      val nonExistContent = mock[ChannelHandlerContext]
      holder.get(nonExistContent) === None
    }

    "get context size" in new Mocking {
      holder.add(mock[ChannelHandlerContext])
      holder.add(mock[ChannelHandlerContext])
      holder.size === 2
    }

    "get all rich context as list" in new Mocking {
      holder.add(context)
      holder.all === List(ContextData(context))
    }

    trait Mocking extends Scope {
      val context = mock[ChannelHandlerContext]
      val rich = new ContextData(context)
      val holder = new Contexts {
        override val contexts = mutable.Map.empty[ChannelHandlerContext, ContextData]
      }
    }
  }

}
