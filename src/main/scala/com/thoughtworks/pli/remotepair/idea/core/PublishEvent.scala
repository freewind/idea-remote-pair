package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.concurrent.{Future, Promise}

case class PublishEvent(currentProject: RichProject) {

  def apply(event: PairEvent): Future[Unit] = {
    currentProject.connection match {
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
