package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.remotepair.server.{Clients, Projects, ServerHandlerProvider}
import com.thoughtworks.pli.intellij.remotepair.{ChangeContentEvent, ChangeMasterEvent, CloseTabEvent, CreateFileEvent, CreateProjectRequest, DeleteFileEvent, JoinProjectRequest, MoveCaretEvent, MyMocking, OpenTabEvent, RenameEvent, ResetContentEvent, ResetTabEvent, SelectContentEvent, _}
import io.netty.channel.ChannelHandlerContext

trait ProtocolMocking extends MyMocking with MockEvents {
  m =>

  private val contexts = new Clients {}

  val projects = new Projects {}
  def dataOf(context: ChannelHandlerContext) = {
    handler.clients.get(context).get
  }

  val handler = new ServerHandlerProvider {
    override val clients = m.contexts
    override val projects = m.projects
  }

  val context1 = mock[ChannelHandlerContext]
  val context2 = mock[ChannelHandlerContext]
  val context3 = mock[ChannelHandlerContext]
  val context4 = mock[ChannelHandlerContext]
  val context5 = mock[ChannelHandlerContext]

  def getClientName(ctx: ChannelHandlerContext) = Map(
    context1 -> "Freewind",
    context2 -> "Lily",
    context3 -> "Mike",
    context4 -> "Jeff",
    context5 -> "Alex"
  ).apply(ctx)

  def client(contexts: ChannelHandlerContext*) = new {

    contexts.foreach { context =>
      if (!handler.clients.contains(context)) {
        handler.channelActive(context)
      }
    }

    private def singleSend(context: ChannelHandlerContext, event: PairEvent) = {
      handler.channelRead(context, event.toMessage)
    }

    def createOrJoinProject(projectName: String): this.type = {
      singleSend(contexts.head, CreateProjectRequest(projectName, getClientName(contexts.head)))
      contexts.tail.foreach(ctx => singleSend(ctx, JoinProjectRequest(projectName, getClientName(ctx))))
      this
    }

    def shareCaret(): this.type = {
      send(CaretSharingModeRequest)
      this
    }

    def parallel(): this.type = {
      send(ParallelModeRequest)
    }

    def send(events: PairEvent*): this.type = {
      for {
        context <- contexts
        event <- events
      } singleSend(context, event)
      this
    }
    def changeMaster(newName: String): this.type = {
      contexts.foreach { context =>
        singleSend(context, ChangeMasterEvent(newName))
      }
      this
    }

    def beMaster(): this.type = {
      contexts.foreach { context =>
        if (!handler.clients.contains(context)) {
          handler.clients.newClient(context)
        }
        handler.clients.all.foreach(_.isMaster = false)
        dataOf(context).isMaster = true
      }
      this
    }
  }

  def project(name: String) = projects.get(name).get

  def clientId(context: ChannelHandlerContext) = handler.clients.get(context).map(_.id).get

  def resetMock(mock: Any) = org.mockito.Mockito.reset(mock)
}

trait MockEvents {
  val changeContentEventA1 = ChangeContentEvent("eventId1", "/aaa", 10, Seq(Insert(2, "abc")))
  val resetContentEvent = ResetContentEvent("/aaa", "new-content", "s4")
  val openTabEvent1 = OpenTabEvent("/aaa")
  val openTabEvent2 = OpenTabEvent("/bbb")
  val closeTabEvent = CloseTabEvent("/aaa")
  val resetTabEvent = ResetTabEvent("/ccc")

  val createFileEvent = CreateFileEvent("/aaa", Content("my-content", "UTF-8"))
  val deleteFileEvent = DeleteFileEvent("/aaa")
  val createDirEvent = CreateFileEvent("/ddd", Content("my-content", "UTF-8"))
  val deleteDirEvent = DeleteFileEvent("/ddd")
  val renameEvent = RenameEvent("/ccc", "/eee")
  val changeMasterEvent = ChangeMasterEvent("Lily")

  val moveCaretEvent1 = MoveCaretEvent("/aaa", 10)
  val moveCaretEvent2 = MoveCaretEvent("/aaa", 20)
  val moveCaretEvent3 = MoveCaretEvent("/bbb", 10)

  val selectContentEvent1 = SelectContentEvent("/aaa", 10, 5)
  val selectContentEvent2 = SelectContentEvent("/aaa", 20, 7)
  val selectContentEvent3 = SelectContentEvent("/bbb", 14, 8)

  val syncFilesRequest = SyncFilesRequest("123", Nil)
}
