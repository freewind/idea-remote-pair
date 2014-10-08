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
      broadcastServerStatusResponse()
    }

    override def channelInactive(ctx: ChannelHandlerContext) {
      contexts.remove(ctx)
      if (!contexts.all.exists(_.master)) {
        contexts.all.headOption.foreach(_.master = true)
      }
      broadcastServerStatusResponse()
    }

    override def channelRead(context: ChannelHandlerContext, msg: Any) = msg match {
      case line: String => contexts.get(context).foreach(data =>
        parseEvent(line) match {
          case event: ClientInfoEvent => handleClientInfoEvent(data, event)
          case event: ChangeMasterEvent => handleChangeMasterEvent(data, event)

          case event: OpenTabEvent => handleOpenTabEvent(data, event)
          case event: ResetTabEvent => handleResetTabEvent(data, event)

          case event: ChangeContentEvent => handleChangeContentEvent(data, event)
          case event: ResetContentEvent => handleResetContentEvent(data, event)

          case event: MoveCaretEvent => handleMoveCaretEvent(data, event)
          case event: ResetCaretEvent => handleResetCaretEvent(data, event)

          case event@(_: CreateFileEvent | _: DeleteFileEvent | _: CreateDirEvent | _: DeleteDirEvent | _: RenameEvent) => broadcastThen(data, event)(identity)

          case _ =>
        }
      )
      case _ => println("### unknown msg type: " + msg)
    }

    def handleResetTabEvent(data: ContextData, event: ResetTabEvent) {
      contexts.all.foreach(_.projectSpecifiedLocks.activeTabLocks.clear())
      broadcastThen(data, event)(_.projectSpecifiedLocks.activeTabLocks.add(event.path))
    }

    def handleResetContentEvent(data: ContextData, event: ResetContentEvent) {
      contexts.all.foreach(_.pathSpecifiedLocks.get(event.path).foreach(_.contentLocks.clear()))
      broadcastThen(data, event)(_.pathSpecifiedLocks.get(event.path).foreach(_.contentLocks.add(event.summary)))
    }

    def handleMoveCaretEvent(data: ContextData, event: MoveCaretEvent) {
      def caretLocks(data: ContextData) = data.pathSpecifiedLocks.getOrCreate(event.path).caretLocks
      val locks = caretLocks(data)
      locks.headOption match {
        case Some(x) if x == event.offset => locks.removeHead()
        case Some(_) => sendToMaster(new ResetCaretRequest(event.path))
        case _ => broadcastThen(data, event)(caretLocks(_).add(event.offset))
      }
    }

    def handleResetCaretEvent(data: ContextData, event: ResetCaretEvent) {
      contexts.all.foreach(_.pathSpecifiedLocks.get(event.path).foreach(_.caretLocks.clear()))
      broadcastThen(data, event)(_.pathSpecifiedLocks.get(event.path).foreach(_.caretLocks.add(event.offset)))
    }

    def handleChangeContentEvent(data: ContextData, event: ChangeContentEvent) {
      val locks = data.pathSpecifiedLocks.getOrCreate(event.path).contentLocks
      locks.headOption match {
        case Some(x) if x == event.summary => locks.removeHead()
        case Some(_) => sendToMaster(new ResetContentRequest(event.path))
        case _ => broadcastThen(data, event)(_.pathSpecifiedLocks.getOrCreate(event.path).contentLocks.add(event.summary))
      }
    }

    def handleOpenTabEvent(data: ContextData, event: OpenTabEvent) {
      val locks = data.projectSpecifiedLocks.activeTabLocks
      locks.headOption match {
        case Some(x) if x == event.path => locks.removeHead()
        case Some(_) => sendToMaster(new ResetTabRequest())
        case _ => broadcastThen(data, event)(_.projectSpecifiedLocks.activeTabLocks.add(event.path))
      }
    }

    private def broadcastThen(data: ContextData, pairEvent: PairEvent)(f: ContextData => Any) {
      contexts.all.filter(_.context != data.context).foreach { otherData =>
        otherData.writeEvent(pairEvent)
        f(otherData)
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
        case "DeleteFileEvent" => Serialization.read[DeleteFileEvent](json)
        case "CreateDirEvent" => Serialization.read[CreateDirEvent](json)
        case "DeleteDirEvent" => Serialization.read[DeleteDirEvent](json)
        case "RenameEvent" => Serialization.read[RenameEvent](json)
        case "MoveCaretEvent" => Serialization.read[MoveCaretEvent](json)
        case "ResetCaretEvent" => Serialization.read[ResetCaretEvent](json)
        case _ =>
          println("##### unknown line: " + line)
          new NoopEvent
      }
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      cause.printStackTrace()
    }

    def handleChangeMasterEvent(data: ContextData, event: ChangeMasterEvent) {
      if (contexts.all.exists(_.name == event.name)) {
        contexts.all.foreach(d => d.master = d.name == event.name)
        broadcastServerStatusResponse()
      } else {
        data.writeEvent(ServerErrorResponse(s"Specified user '${event.name}' is not found"))
      }
    }

    def handleClientInfoEvent(data: ContextData, event: ClientInfoEvent) {
      val name = event.name.trim
      if (name.isEmpty) {
        data.writeEvent(ServerErrorResponse("Name is not provided"))
      } else if (contexts.all.exists(_.name == name)) {
        data.writeEvent(ServerErrorResponse(s"Specified name '$name' is already existing"))
      } else {
        data.name = event.name
        data.ip = event.ip
        broadcastServerStatusResponse()
      }
    }

    private def broadcastServerStatusResponse() {
      val clients = contexts.all.map(d => ClientInfoData(d.ip, d.name, d.master))
      val event = ServerStatusResponse(clients)
      contexts.all.foreach(_.writeEvent(event))
    }
  }

  def sendToMaster(resetEvent: PairEvent) {
    contexts.all.find(_.master).foreach(_.writeEvent(resetEvent))
  }
}
