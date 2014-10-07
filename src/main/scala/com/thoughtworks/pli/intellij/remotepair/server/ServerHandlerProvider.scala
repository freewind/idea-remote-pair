package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel._
import com.thoughtworks.pli.intellij.remotepair._
import net.liftweb.json.{DefaultFormats, Serialization}
import com.thoughtworks.pli.intellij.remotepair.OpenTabEvent
import com.thoughtworks.pli.intellij.remotepair.NoopEvent
import com.thoughtworks.pli.intellij.remotepair.ChangeContentEvent
import scala.Some
import com.thoughtworks.pli.intellij.remotepair.ClientInfoEvent

trait ServerHandlerProvider {
  this: ContextHolderProvider =>

  def createServerHandler() = new MyServerHandler

  class MyServerHandler extends ChannelHandlerAdapter {
    override def channelActive(ctx: ChannelHandlerContext) {
      val data = contexts.add(ctx)
      if (contexts.size == 1) {
        data.master = true
      }
    }

    override def channelInactive(ctx: ChannelHandlerContext) {
      contexts.remove(ctx)
      if (!contexts.allData.exists(_.master)) {
        contexts.allData.headOption.foreach(_.master = true)
      }
    }

    override def channelRead(context: ChannelHandlerContext, msg: Any) = msg match {
      case line: String => contexts.get(context).foreach(data =>
        parseEvent(line) match {
          case event: ClientInfoEvent => handleClientInfoEvent(data, event)
          case event: ChangeMasterEvent => handleChangeMasterEvent(data, event)
          case event: OpenTabEvent => handleOpenTabEvent(data, event)
          case event: ChangeContentEvent => handleChangeContentEvent(data, event)
          case event: ResetContentEvent => handleResetContentEvent(data, event)
          case event: ResetTabEvent => handleResetTabEvent(data, event)
          case event: CreateFileEvent => broadcastThen(data, event)(identity)
          case _ =>
        }
      )
      case _ => println("### unknown msg type: " + msg)
    }


    def handleResetTabEvent(data: ContextData, event: ResetTabEvent) {
      contexts.allData.foreach(_.activeTabLocks.clear())
      broadcastThen(data, event)(_.activeTabLocks.add(event.path))
    }

    def handleResetContentEvent(data: ContextData, event: ResetContentEvent) {
      contexts.allData.foreach(_.contentLocks.get(event.path).foreach(_.clear()))
      broadcastThen(data, event)(_.contentLocks.add(event.path, event.summary))
    }

    def handleChangeContentEvent(data: ContextData, event: ChangeContentEvent) {
      val locksOpt = data.contentLocks.get(event.path)
      locksOpt match {
        case Some(locks) =>
          locks.headOption match {
            case Some(x) if x == event.summary => locks.removeHead()
            case Some(_) =>
              contexts.allData.find(_.master).foreach(_.context.writeEvent(new ResetContentRequest(event.path)))
            case _ => broadcastThen(data, event)(_.contentLocks.add(event.path, event.summary))
          }
        case None => broadcastThen(data, event)(_.contentLocks.add(event.path, event.summary))
      }
    }

    def handleOpenTabEvent(data: ContextData, event: OpenTabEvent) {
      val locks = data.activeTabLocks
      locks.headOption match {
        case Some(x) if x == event.path => locks.removeHead()
        case Some(_) =>
          contexts.allData.find(_.master).foreach(_.context.writeEvent(new ResetTabRequest()))
        case _ => broadcastThen(data, event)(_.activeTabLocks.add(event.path))
      }
    }

    private def broadcastThen(data: ContextData, pairEvent: PairEvent)(f: ContextData => Any) {
      contexts.all.filter(_ != data.context).foreach { otherContext =>
        otherContext.writeEvent(pairEvent)
        contexts.get(otherContext).foreach(f)
      }
    }

    private def parseEvent(line: String) = {
      implicit val formats = DefaultFormats
      val (name, json) = line.span(_ != ' ')
      name match {
        case "ClientInfoEvent" => Serialization.read[ClientInfoEvent](json)
        case "OpenTabEvent" => Serialization.read[OpenTabEvent](json)
        case "ChangeContentEvent" => Serialization.read[ChangeContentEvent](json)
        case "ChangeMasterEvent" => Serialization.read[ChangeMasterEvent](json)
        case "ResetContentEvent" => Serialization.read[ResetContentEvent](json)
        case "ResetTabEvent" => Serialization.read[ResetTabEvent](json)
        case "CreateFileEvent" => Serialization.read[CreateFileEvent](json)
        case _ =>
          println("##### unknown line: " + line)
          new NoopEvent
      }
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      cause.printStackTrace()
    }

    def handleChangeMasterEvent(data: ContextData, event: ChangeMasterEvent) {
      if (contexts.allData.exists(_.name == event.name)) {
        contexts.allData.foreach(d => d.master = d.name == event.name)
      } else {
        data.context.writeEvent(ServerErrorResponse(s"Specified user '${event.name}' is not found"))
      }
    }

    def handleClientInfoEvent(data: ContextData, event: ClientInfoEvent) {
      data.name = event.name
      data.ip = event.ip
    }
  }

}
