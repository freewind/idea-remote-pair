package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.intellij.remotepair.protocol._
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.concurrent.{Future, Promise}

class PublishEvent(connectionHolder: ConnectionHolder) {

  def apply(event: PairEvent): Future[Unit] = {
    connectionHolder.get match {
      case Some(conn) => {
        val p = Promise[Unit]()
        conn.publish(event).addListener(new GenericFutureListener[ChannelFuture] {
          override def operationComplete(f: ChannelFuture): Unit = {
            if (f.cause() != null) {
              p.failure(f.cause())
            } else {
              p.success(())
            }
          }
        })
        p.future
      }
      case _ => Future.failed(new IllegalStateException("No server connection available"))
    }
  }

}
