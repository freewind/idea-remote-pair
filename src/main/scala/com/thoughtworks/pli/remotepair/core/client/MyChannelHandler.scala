package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.server_event_handlers.HandleEvent
import com.thoughtworks.pli.remotepair.core.{ConnectionHolder, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import io.netty.channel._

object MyChannelHandler {
  type Factory = () => MyChannelHandler
}

class MyChannelHandler(connectionHolder: ConnectionHolder, handleEvent: HandleEvent, pairEventListeners: PairEventListeners, connectionFactory: Connection.Factory, logger: PluginLogger) extends ChannelHandlerAdapter {

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    connectionHolder.put(Some(connectionFactory(ctx)))
  }
  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    val event = msg.asInstanceOf[PairEvent]
    handleEvent(event)
    pairEventListeners.triggerReadMonitors(event)
  }
  override def write(ctx: ChannelHandlerContext, msg: scala.Any, promise: ChannelPromise): Unit = {
    val event = msg.asInstanceOf[PairEvent]
    pairEventListeners.triggerWrittenMonitors(event)
  }
  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    connectionHolder.put(None)
  }
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close()
  }

}
