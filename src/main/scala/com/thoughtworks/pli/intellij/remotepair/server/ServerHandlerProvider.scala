package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel._
import com.thoughtworks.pli.intellij.remotepair._
import net.liftweb.json.{DefaultFormats, Serialization}
import com.thoughtworks.pli.intellij.remotepair.OpenTabEvent
import com.thoughtworks.pli.intellij.remotepair.NoopEvent
import com.thoughtworks.pli.intellij.remotepair.ContentChangeEvent
import scala.Some
import com.thoughtworks.pli.intellij.remotepair.NewClientEvent

trait ServerHandlerProvider {
  this: ContextHolderProvider =>

  def createServerHandler() = new MyServerHandler

  class MyServerHandler extends ChannelHandlerAdapter {
    override def channelActive(ctx: ChannelHandlerContext) {
      val rich = contexts.add(ctx)
      if (contexts.size == 1) {
        rich.master = true
      }
    }

    override def channelInactive(ctx: ChannelHandlerContext) {
      contexts.remove(ctx)
      if (!contexts.allData.exists(_.master)) {
        contexts.allData.headOption.foreach(_.master = true)
      }
    }

    override def channelRead(context: ChannelHandlerContext, msg: Any) = {
      println(s"######### get from client: msg type [${msg.getClass.getSimpleName}]")
      println(s"[$msg]")
      msg match {
        case line: String =>

          contexts.get(context).foreach { xxx =>
            implicit val formats = DefaultFormats
            val (name, json) = line.span(_ != ' ')
            val event = name match {
              case "NewClientEvent" => val event = Serialization.read[NewClientEvent](json)
                println("************** New client from: " + event)
                event
              case "OpenTabEvent" => val event = Serialization.read[OpenTabEvent](json)
                println("************** openTabEvent: " + event)
                event
              case "ContentChangeEvent" => val event = Serialization.read[ContentChangeEvent](json)
                println("************** ModifyContentEvent: " + event)
                event
              case "BeMasterEvent" =>
                contexts.allData.foreach(rich => rich.master = rich.context == context)
                new NoopEvent
              case "ResetContentEvent" =>
                val event = Serialization.read[ResetContentEvent](json)
                contexts.allData.foreach { data =>
                  data.contentLocks.get(event.path).foreach(_.clear())
                }
                event
              case "ResetTabEvent" =>
                val event = Serialization.read[ResetTabEvent](json)
                contexts.allData.foreach { data =>
                  data.activeTabLocks.clear()
                }
                event
              case _ => println("##### unknown line: " + line)
                new NoopEvent
            }

            contexts.get(context).foreach { data =>
              event match {
                case ee: OpenTabEvent =>
                  val locks = data.activeTabLocks
                  locks.headOption match {
                    case Some(x) if x == ee.path => locks.removeHead()
                    case Some(_) =>
                      contexts.allData.find(_.master).foreach(_.context.writeEvent(new TabResetRequestEvent()))
                    case _ => broadcastThen(_.activeTabLocks.add(ee.path))
                  }
                case ee: ContentChangeEvent =>
                  val locksOpt = data.contentLocks.get(ee.path)
                  locksOpt match {
                    case Some(locks) =>
                      locks.headOption match {
                        case Some(x) if x == ee.summary => locks.removeHead()
                        case Some(_) =>
                          contexts.allData.find(_.master).foreach(_.context.writeEvent(new ContentResetRequestEvent(ee.path)))
                        case _ => broadcastThen(_.contentLocks.add(ee.path, ee.summary))
                      }
                    case None => broadcastThen(_.contentLocks.add(ee.path, ee.summary))
                  }
                case ee: ResetContentEvent => broadcastThen(_.contentLocks.add(ee.path, ee.summary))
                case ee: ResetTabEvent => broadcastThen(_.activeTabLocks.add(ee.path))
                case _ =>
              }
            }

            def broadcastThen(f: ContextData => Any) {
              contexts.all.filter(_ != context).foreach { otherContext =>
                otherContext.writeLineAndFlush(line)
                contexts.get(otherContext).foreach(f)
              }
            }
          }

        case _ => println("### unknown msg type: " + msg)
      }
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      cause.printStackTrace()
    }
  }

}
