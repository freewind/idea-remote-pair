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
  this: ContextHolderProvider with ClientModeGroups with ProjectsHolder =>

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
          case event: CloseTabEvent => handleCloseTabEvent(data, event)
          case event: ResetTabEvent => handleResetTabEvent(data, event)

          case event: ChangeContentEvent => handleChangeContentEvent(data, event)
          case event: ResetContentEvent => handleResetContentEvent(data, event)

          case event: MoveCaretEvent => handleMoveCaretEvent(data, event)
          case event: ResetCaretEvent => handleResetCaretEvent(data, event)

          case event: SelectContentEvent => handleSelectContentEvent(data, event)
          case req: ResetSelectionEvent => handleResetSelectionEvent(data, req)

          case event@(_: CreateFileEvent | _: DeleteFileEvent | _: CreateDirEvent | _: DeleteDirEvent | _: RenameEvent) => broadcastThen(data, event)(identity)

          case event: IgnoreFilesRequest => handleIgnoreFilesRequest(event)

          case req: BindModeRequest => handleBindModeRequest(data, req)
          case req: FollowModeRequest => handleFollowModeRequest(data, req)

          case req: CreateProjectRequest => handleCreateProjectRequest(data, req)
          case req: JoinProjectRequest => handleJoinProjectRequest(data, req)
          case request: SyncFilesRequest => handleSyncFilesRequest(request)
          case _ =>
        }
      )
      case _ => throw new Exception("### unknown msg type: " + msg)
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

    def handleSelectContentEvent(data: ContextData, event: SelectContentEvent) {
      def selectionLocks(data: ContextData) = data.pathSpecifiedLocks.getOrCreate(event.path).selectionLocks
      val range = SelectionRange(event.offset, event.length)

      val locks = selectionLocks(data)
      locks.headOption match {
        case Some(x) if x == range => locks.removeHead()
        case Some(_) => sendToMaster(new ResetSelectionRequest(event.path))
        case _ => broadcastThen(data, event)(selectionLocks(_).add(range))
      }
    }

    def handleResetSelectionEvent(data: ContextData, event: ResetSelectionEvent) {
      val range = SelectionRange(event.offset, event.length)
      contexts.all.foreach(_.pathSpecifiedLocks.get(event.path).foreach(_.selectionLocks.clear()))
      broadcastThen(data, event)(_.pathSpecifiedLocks.get(event.path).foreach(_.selectionLocks.add(range)))
    }

    def handleIgnoreFilesRequest(request: IgnoreFilesRequest) {
      ignoredFiles = request.files
      broadcastServerStatusResponse()
    }

    def handleBindModeRequest(context: ContextData, request: BindModeRequest) {
      if (context.name == request.name) {
        context.writeEvent(ServerErrorResponse("Can't bind to self"))
      } else if (!contexts.all.exists(_.name == request.name)) {
        context.writeEvent(ServerErrorResponse(s"Can't bind to non-exist user: '${request.name}'"))
      } else {
        val all = bindModeGroups.flatten.toList
        Seq(request.name, context.name).foreach { name =>
          if (!all.contains(name)) {
            bindModeGroups = Set(name) :: bindModeGroups
          }
        }
        bindModeGroups.map { g =>
          var group = if (g.contains(context.name)) {
            g - context.name
          } else g
          group = if (group.contains(request.name)) {
            group + context.name
          } else group
          group
        }.filterNot(_.isEmpty).span(_.size > 1) match {
          case (multis, singles) => bindModeGroups = multis
        }

        followModeMap = followModeMap.map {
          case (key, values) => key -> (values - context.name)
        }.filterNot(_._2.isEmpty)
      }
    }

    def handleFollowModeRequest(context: ContextData, request: FollowModeRequest) {
      def isFollower(fanName: String, heroName: String) = {
        followModeMap.exists {
          case (k, v) => k == heroName && v.contains(fanName)
        }
      }
      if (context.name == request.name) {
        context.writeEvent(ServerErrorResponse("Can't follow self"))
      } else if (!contexts.all.exists(_.name == request.name)) {
        context.writeEvent(ServerErrorResponse(s"Can't follow non-exist user: '${request.name}'"))
      } else if (isFollower(request.name, context.name)) {
        context.writeEvent(ServerErrorResponse(s"Can't follow your follower: '${request.name}'"))
      } else {
        var fans: Set[String] = Set.empty
        followModeMap.span(_._1 == context.name) match {
          case (found, others) =>
            if (found.isEmpty) {
              fans = Set(context.name)
              followModeMap = others.map {
                case (key, values) if values.contains(context.name) => key -> (values - context.name)
                case kv => kv
              }
            } else {
              fans = {
                val h = found.toList.head
                h._2 + h._1
              }
              followModeMap = others
            }
        }
        var hero = request.name
        followModeMap.find(_._2.contains(request.name)) match {
          case Some(kv) => hero = kv._1
          case _ =>
        }

        followModeMap = ((hero -> fans) :: followModeMap.toList).groupBy(_._1).map {
          case (key, values) => key -> values.map(_._2).flatten.toSet
        }
        followModeMap = followModeMap.filterNot(_._2.isEmpty)

        bindModeGroups = bindModeGroups.map(_ - context.name).filter(_.size > 1)
      }
    }

    def handleCreateProjectRequest(data: ContextData, request: CreateProjectRequest) {
      if (data.hasUserInformation) {
        val projectName = request.name
        if (projects.contains(projectName)) {
          data.writeEvent(ServerErrorResponse(s"Project '$projectName' is already exist, can't create again"))
        } else {
          createOrJoinProject(data, projectName)
        }
      } else {
        data.writeEvent(ServerErrorResponse("Please tell me your information first"))
      }
    }

    def handleJoinProjectRequest(data: ContextData, request: JoinProjectRequest) {
      if (data.hasUserInformation) {
        val projectName = request.name
        if (!projects.contains(projectName)) {
          data.writeEvent(ServerErrorResponse(s"You can't join a non-existent project: '$projectName'"))
        } else {
          createOrJoinProject(data, projectName)
        }
      } else {
        data.writeEvent(ServerErrorResponse("Please tell me your information first"))
      }

    }

    def handleSyncFilesRequest(request: SyncFilesRequest) {
      sendToMaster(request)
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

    def handleCloseTabEvent(data: ContextData, event: CloseTabEvent) {
      broadcastThen(data, event)(identity)
    }

    private def broadcastThen(data: ContextData, pairEvent: PairEvent)(f: ContextData => Any) {
      val senderName = data.name
      def projectMembers = projects.find(_._2.contains(senderName)).fold(Set.empty[String])(_._2)
      contexts.all.filter(x => projectMembers.contains(x.name)).filter(_.context != data.context).foreach { otherData =>
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
        case "CloseTabEvent" => Serialization.read[CloseTabEvent](json)
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
        case "SelectContentEvent" => Serialization.read[SelectContentEvent](json)
        case "ResetSelectionEvent" => Serialization.read[ResetSelectionEvent](json)
        case "IgnoreFilesRequest" => Serialization.read[IgnoreFilesRequest](json)
        case "SyncFilesRequest" => Serialization.read[SyncFilesRequest](json)
        case "BindModeRequest" => Serialization.read[BindModeRequest](json)
        case "FollowModeRequest" => Serialization.read[FollowModeRequest](json)
        case "CreateProjectRequest" => Serialization.read[CreateProjectRequest](json)
        case "JoinProjectRequest" => Serialization.read[JoinProjectRequest](json)
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
      val event = ServerStatusResponse(clients, ignoredFiles)
      contexts.all.foreach(_.writeEvent(event))
    }
  }

  def createOrJoinProject(data: ContextData, projectName: String) {
    def removeUser(projects: Map[String, Set[String]], name: String) = projects.map(kv => kv._1 -> (kv._2 - name)).filter(_._2.size > 0)
    projects = (projectName -> Set(data.name) :: removeUser(projects, data.name).toList)
      .groupBy(_._1).map(kv => kv._1 -> kv._2.map(_._2).reduce(_ ++ _))
  }

  def sendToMaster(resetEvent: PairEvent) {
    contexts.all.find(_.master).foreach(_.writeEvent(resetEvent))
  }
}
