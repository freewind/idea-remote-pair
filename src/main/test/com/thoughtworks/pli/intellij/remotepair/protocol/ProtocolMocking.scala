package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.MyMocking
import com.thoughtworks.pli.intellij.remotepair.{ChangeContentEvent, ChangeMasterEvent, ClientInfoEvent, CloseTabEvent, CreateFileEvent, CreateProjectRequest, DeleteFileEvent, JoinProjectRequest, MoveCaretEvent, OpenTabEvent, RenameEvent, ResetContentEvent, ResetTabEvent, SelectContentEvent, _}
import com.thoughtworks.pli.intellij.remotepair.server.{Contexts, Projects, ServerHandlerProvider}
import io.netty.channel.ChannelHandlerContext

trait ProtocolMocking extends MyMocking with MockEvents {
  m =>

  private val contexts = new Contexts {}
  val projects = new Projects {}
  def dataOf(context: ChannelHandlerContext) = {
    handler.contexts.get(context).get
  }

  val handler = new ServerHandlerProvider {
    override val contexts = m.contexts
    override val projects = m.projects
  }

  val context1 = mock[ChannelHandlerContext]
  val context2 = mock[ChannelHandlerContext]
  val context3 = mock[ChannelHandlerContext]
  val context4 = mock[ChannelHandlerContext]
  val context5 = mock[ChannelHandlerContext]

  val contextWithInfo = Map(
    context1 -> clientInfoEvent1,
    context2 -> clientInfoEvent2,
    context3 -> clientInfoEvent3,
    context4 -> clientInfoEvent4,
    context5 -> clientInfoEvent5
  )

  def client(contexts: ChannelHandlerContext*) = new {
    private def singleSend(context: ChannelHandlerContext, event: PairEvent) = {
      handler.channelRead(context, event.toMessage)
    }

    def active(sendInfo: Boolean): this.type = {
      contexts.toList.filterNot(handler.contexts.contains).foreach { ctx =>
        handler.channelActive(ctx)
        if (sendInfo) {
          singleSend(ctx, contextWithInfo(ctx))
        }
      }
      this
    }

    def joinProject(projectName: String): this.type = {
      singleSend(contexts.head, CreateProjectRequest(projectName))
      contexts.tail.foreach(ctx => singleSend(ctx, JoinProjectRequest(projectName)))
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
    def beMaster(): this.type = {
      contexts.foreach { context =>
        if (!handler.contexts.contains(context)) {
          handler.contexts.add(context)
        }
        handler.contexts.all.foreach(_.master = false)
        dataOf(context).master = true
      }
      this
    }
  }

  def project(name: String) = projects.get(name).get

  def resetMock(mock: Any) = org.mockito.Mockito.reset(mock)
}

trait MockEvents {
  val changeContentEventA1 = ChangeContentEvent("/aaa", 10, "aa1", "bb1", "s1")
  val changeContentEventA1SameSummary = ChangeContentEvent("/aaa", 100, "aaaaaa1", "bbbbbbbbb1", "s1")
  val changeContentEventA2 = ChangeContentEvent("/aaa", 20, "aa2", "bb2", "s2")
  val changeContentEventB1 = ChangeContentEvent("/bbb", 30, "aa3", "bb3", "s3")
  val resetContentEvent = ResetContentEvent("/aaa", "new-content", "s4")
  val openTabEvent1 = OpenTabEvent("/aaa")
  val openTabEvent2 = OpenTabEvent("/bbb")
  val closeTabEvent = CloseTabEvent("/aaa")
  val resetTabEvent = ResetTabEvent("/ccc")

  val clientInfoEvent1 = ClientInfoEvent("1.1.1.1", "Freewind")
  val clientInfoEvent2 = ClientInfoEvent("2.2.2.2", "Lily")
  val clientInfoEvent3 = ClientInfoEvent("3.3.3.3", "Mike")
  val clientInfoEvent4 = ClientInfoEvent("4.4.4.4", "Jeff")
  val clientInfoEvent5 = ClientInfoEvent("5.5.5.5", "Alex")

  val createFileEvent = CreateFileEvent("/aaa")
  val deleteFileEvent = DeleteFileEvent("/aaa")
  val createDirEvent = CreateFileEvent("/ddd")
  val deleteDirEvent = DeleteFileEvent("/ddd")
  val renameEvent = RenameEvent("/ccc", "/eee")
  val changeMasterEvent = ChangeMasterEvent("Lily")

  val moveCaretEvent1 = MoveCaretEvent("/aaa", 10)
  val moveCaretEvent2 = MoveCaretEvent("/aaa", 20)
  val moveCaretEvent3 = MoveCaretEvent("/bbb", 10)

  val selectContentEvent1 = SelectContentEvent("/aaa", 10, 5)
  val selectContentEvent2 = SelectContentEvent("/aaa", 20, 7)
  val selectContentEvent3 = SelectContentEvent("/bbb", 14, 8)

  val syncFilesRequest = SyncFilesRequest
}
