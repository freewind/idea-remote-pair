package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel._
import com.thoughtworks.pli.intellij.remotepair._
import scala.Some
import com.thoughtworks.pli.intellij.remotepair.ClientInfoEvent

trait ServerHandlerProvider extends EventParser with ClientModeGroups with ProjectsHolder with ContextHolder {

  def createServerHandler() = new MyServerHandler

  class MyServerHandler extends ChannelHandlerAdapter {
    override def channelActive(ctx: ChannelHandlerContext) {
      contexts.add(ctx)
      contexts.get(ctx).foreach(askForLogin)
      broadcastServerStatusResponse()
    }

    private def askForLogin(data: ContextData) {
      if (!data.hasUserInformation) {
        data.writeEvent(AskForClientInformation())
      } else if (findProjectForUser(data.name).isEmpty) {
        data.writeEvent(AskForJoinProject())
      } else if (!hasWorkingMode(data.name)) {
        data.writeEvent(AskForWorkingMode())
      }
    }

    private def hasWorkingMode(name: String): Boolean = {
      (caretSharingModeGroups.flatten ++ followModeMap.values.flatten ++ parallelModeClients).contains(name)
    }

    override def channelInactive(ctx: ChannelHandlerContext) {
      contexts.get(ctx).foreach { c =>
        unbindUser(c.name)
        unfollow(c.name)
        cantBeFollowed(c.name)
      }

      contexts.remove(ctx)
      if (!contexts.all.exists(_.master)) {
        contexts.all.headOption.foreach(_.master = true)
      }

      broadcastServerStatusResponse()
    }

    override def channelRead(context: ChannelHandlerContext, msg: Any) = msg match {
      case line: String => contexts.get(context).foreach { data =>
        println("####### get line from client " + data.name + ": " + line)
        parseEvent(line) match {
          case event: LoginEvent => {
            event match {
              case event: ClientInfoEvent => handleClientInfoEvent(data, event)
              case req: CreateProjectRequest => handleCreateProjectRequest(data, req)
              case req: JoinProjectRequest => handleJoinProjectRequest(data, req)
              case req: WorkingModeEvent => if (findProjectForUser(data.name).isDefined) {
                req match {
                  case req: CaretSharingModeRequest => handleCaretSharingModeRequest(data, req)
                  case req: FollowModeRequest => handleFollowModeRequest(data, req)
                  case req: ParallelModeRequest => handleParallelModeRequest(data, req)
                }
              } else {
                data.writeEvent(ServerErrorResponse("Operation is not allowed because you have not joined in any project"))
              }
            }
            askForLogin(data)
          }

          case others => if (findProjectForUser(data.name).isDefined) {
            others match {
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

              case event@(_: CreateFileEvent | _: DeleteFileEvent | _: CreateDirEvent | _: DeleteDirEvent | _: RenameEvent) => broadcastToSameProjectMembersThen(data, event)(identity)

              case event: IgnoreFilesRequest => handleIgnoreFilesRequest(data, event)

              case request: SyncFilesRequest => handleSyncFilesRequest(request)
              case _ =>
            }
          } else {
            data.writeEvent(ServerErrorResponse("Operation is not allowed because you have not joined in any project"))
          }
        }
      }
      case _ => throw new Exception("### unknown msg type: " + msg)
    }

    def handleResetTabEvent(data: ContextData, event: ResetTabEvent) {
      contexts.all.foreach(_.projectSpecifiedLocks.activeTabLocks.clear())
      broadcastToSameProjectMembersThen(data, event)(_.projectSpecifiedLocks.activeTabLocks.add(event.path))
    }

    def handleResetContentEvent(data: ContextData, event: ResetContentEvent) {
      contexts.all.foreach(_.pathSpecifiedLocks.get(event.path).foreach(_.contentLocks.clear()))
      broadcastToSameProjectMembersThen(data, event)(_.pathSpecifiedLocks.get(event.path).foreach(_.contentLocks.add(event.summary)))
    }

    def handleMoveCaretEvent(data: ContextData, event: MoveCaretEvent) {
      def caretLocks(data: ContextData) = data.pathSpecifiedLocks.getOrCreate(event.path).caretLocks
      val locks = caretLocks(data)
      locks.headOption match {
        case Some(x) if x == event.offset => locks.removeHead()
        case Some(_) => sendToMaster(new ResetCaretRequest(event.path))
        case _ => broadcastToSameProjectMembersThen(data, event)(caretLocks(_).add(event.offset))
      }
    }

    def handleResetCaretEvent(data: ContextData, event: ResetCaretEvent) {
      contexts.all.foreach(_.pathSpecifiedLocks.get(event.path).foreach(_.caretLocks.clear()))
      broadcastToSameProjectMembersThen(data, event)(_.pathSpecifiedLocks.get(event.path).foreach(_.caretLocks.add(event.offset)))
    }

    def handleSelectContentEvent(data: ContextData, event: SelectContentEvent) {
      def selectionLocks(data: ContextData) = data.pathSpecifiedLocks.getOrCreate(event.path).selectionLocks
      val range = SelectionRange(event.offset, event.length)

      val locks = selectionLocks(data)
      locks.headOption match {
        case Some(x) if x == range => locks.removeHead()
        case Some(_) => sendToMaster(new ResetSelectionRequest(event.path))
        case _ => broadcastToSameProjectMembersThen(data, event)(selectionLocks(_).add(range))
      }
    }

    def handleResetSelectionEvent(data: ContextData, event: ResetSelectionEvent) {
      val range = SelectionRange(event.offset, event.length)
      contexts.all.foreach(_.pathSpecifiedLocks.get(event.path).foreach(_.selectionLocks.clear()))
      broadcastToSameProjectMembersThen(data, event)(_.pathSpecifiedLocks.get(event.path).foreach(_.selectionLocks.add(range)))
    }

    def handleIgnoreFilesRequest(data: ContextData, request: IgnoreFilesRequest) {
      val projectName = findProjectForUser(data.name).get
      projects = projects.map {
        case (k, p) if k == projectName => k -> p.copy(ignoredFiles = request.files)
        case o => o
      }
      broadcastServerStatusResponse()
    }

    private def findProjectForUser(name: String) = projects.find(_._2.members.contains(name)).map(_._1)

    private def inTheSameProject(user1: String, user2: String) = projects.exists(p => p._2.members.contains(user1) && p._2.members.contains(user2))

    def handleCaretSharingModeRequest(context: ContextData, request: CaretSharingModeRequest) {
      if (findProjectForUser(context.name).isEmpty) {
        context.writeEvent(ServerErrorResponse("Operation is not allowed because you have not joined in any project"))
      } else if (context.name == request.name) {
        context.writeEvent(ServerErrorResponse("Can't bind to self"))
      } else if (!contexts.all.exists(_.name == request.name)) {
        context.writeEvent(ServerErrorResponse(s"Can't bind to non-exist user: '${request.name}'"))
      } else if (!inTheSameProject(context.name, request.name)) {
        context.writeEvent(ServerErrorResponse(s"Operation is failed because '${request.name}' is not in the same project"))
      } else {
        val all = caretSharingModeGroups.flatten.toList
        Seq(request.name, context.name).foreach { name =>
          if (!all.contains(name)) {
            caretSharingModeGroups = Set(name) :: caretSharingModeGroups
          }
        }
        caretSharingModeGroups.map { g =>
          var group = if (g.contains(context.name)) {
            g - context.name
          } else g
          group = if (group.contains(request.name)) {
            group + context.name
          } else group
          group
        }.filterNot(_.isEmpty).span(_.size > 1) match {
          case (multis, singles) => caretSharingModeGroups = multis
        }

        val membersInSameBindingGroup = caretSharingModeGroups.find(_.contains(context.name)).getOrElse(Set.empty)
        followModeMap = followModeMap.map {
          case (key, values) => key -> (values -- membersInSameBindingGroup)
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

        caretSharingModeGroups = caretSharingModeGroups.map(_ - context.name).filter(_.size > 1)
      }
    }

    private def isBinding(name: String) = caretSharingModeGroups.exists(_.contains(name))

    private def unbindUser(name: String) {
      caretSharingModeGroups = caretSharingModeGroups.map(_ - name).filter(_.size > 1)
    }

    private def isFollowingOthers(name: String) = followModeMap.exists(_._2.contains(name))

    private def unfollow(name: String) {
      followModeMap = followModeMap.map(kv => kv._1 -> (kv._2 - name)).filterNot(_._2.isEmpty)
    }

    private def cantBeFollowed(name: String) {
      followModeMap = followModeMap.filter(_._1 != name)
    }

    def handleParallelModeRequest(data: ContextData, request: ParallelModeRequest) {
      if (isBinding(data.name)) {
        unbindUser(data.name)
      } else if (isFollowingOthers(data.name)) {
        unfollow(data.name)
      }
    }

    def handleCreateProjectRequest(data: ContextData, request: CreateProjectRequest) {
      if (data.hasUserInformation) {
        val projectName = request.name
        if (projects.contains(projectName)) {
          data.writeEvent(ServerErrorResponse(s"Project '$projectName' is already exist, can't create again"))
        } else {
          createOrJoinProject(data, projectName)

          if (projects.exists(_._2.members == Set(data.name))) {
            data.master = true
            broadcastServerStatusResponse()
          }
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
        case _ => broadcastToSameProjectMembersThen(data, event)(_.pathSpecifiedLocks.getOrCreate(event.path).contentLocks.add(event.summary))
      }
    }

    def handleOpenTabEvent(data: ContextData, event: OpenTabEvent) {
      val locks = data.projectSpecifiedLocks.activeTabLocks
      locks.headOption match {
        case Some(x) if x == event.path => locks.removeHead()
        case Some(_) => sendToMaster(new ResetTabRequest())
        case _ => broadcastToSameProjectMembersThen(data, event)(_.projectSpecifiedLocks.activeTabLocks.add(event.path))
      }
    }

    def handleCloseTabEvent(data: ContextData, event: CloseTabEvent) {
      broadcastToSameProjectMembersThen(data, event)(identity)
    }

    private def broadcastToSameProjectMembersThen(data: ContextData, pairEvent: PairEvent)(f: ContextData => Any) {
      val senderName = data.name
      def projectMembers = projects.find(_._2.members.contains(senderName)).fold(Set.empty[String])(_._2.members)
      contexts.all.filter(x => projectMembers.contains(x.name)).filter(_.context != data.context).foreach { otherData =>
        def doit() {
          otherData.writeEvent(pairEvent)
          f(otherData)
        }
        val otherName = otherData.name

        if (isFollowingOthers(senderName)) {
          // don't broadcast anything
        } else {
          pairEvent match {
            case _: ChangeContentEvent | _: ResetContentEvent => doit()
            case _: CreateFileEvent | _: DeleteFileEvent | _: CreateDirEvent | _: DeleteDirEvent | _: RenameEvent => doit()
            case x if areBinding(senderName, otherName) || isFollowing(otherName, senderName) => doit()
            case _ =>
          }
        }
      }

    }

    private def areBinding(name1: String, name2: String) = caretSharingModeGroups.exists(g => g.contains(name1) && g.contains(name2))

    private def isFollowing(follower: String, hero: String) = followModeMap.exists(kv => kv._1 == hero && kv._2.contains(follower))


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

    private def findContextByUserName(name: String) = contexts.findByUserName(name)

    private def broadcastServerStatusResponse() {
      def client2data(d: ContextData) = ClientInfoData(d.ip, d.name, d.master)
      val ps = projects.values.map(p => ProjectInfoData(p.name, p.members.flatMap(findContextByUserName).map(client2data), p.ignoredFiles)).toList
      val freeClients = contexts.all.filter(c => findProjectForUser(c.name).isEmpty).map(client2data)
      val event = ServerStatusResponse(ps, freeClients)
      contexts.all.foreach(_.writeEvent(event))
    }
  }

  def createOrJoinProject(data: ContextData, projectName: String) {
    val userName = data.name
    def removeUser(projects: Map[String, Project], name: String) = projects.map(kv => kv._1 -> {
      val p = kv._2
      p.copy(members = p.members - name)
    })
    projects = removeUser(projects, userName).map {
      case (n, project) if n == projectName => n -> project.copy(members = project.members + data.name)
      case o => o
    }.filter(_._2.members.size > 0)
    if (!projects.contains(projectName)) {
      projects = projects + (projectName -> Project(projectName, Set(data.name), Nil))
    }
    println("#########################")
    println(projects)
  }

  def sendToMaster(resetEvent: PairEvent) {
    contexts.all.find(_.master).foreach(_.writeEvent(resetEvent))
  }
}
