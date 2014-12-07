package com.thoughtworks.pli.intellij.remotepair.server

import com.thoughtworks.pli.intellij.remotepair._
import io.netty.channel._

class ServerHandlerProvider extends ChannelHandlerAdapter with EventParser {
  val clients: Clients = Clients
  val projects: Projects = Projects

  override def channelActive(ctx: ChannelHandlerContext) {
    val client = clients.newClient(ctx)
    broadcastServerStatusResponse(Some(client))
    client.writeEvent(AskForJoinProject())
  }

  override def channelInactive(ctx: ChannelHandlerContext) {
    def removeFromProject(client: Client): Unit = {
      projects.findForClient(client).foreach { project =>
        project.removeMember(client)
        if (client.isMaster) {
          project.members.headOption.foreach(_.isMaster = true)
        }
      }
    }

    removeFromProject(clients.get(ctx).get)
    clients.removeClient(ctx)
    broadcastServerStatusResponse(None)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    clients.get(ctx).foreach(_.writeEvent(ServerErrorResponse(cause.toString)))
  }

  override def channelRead(context: ChannelHandlerContext, msg: Any) = msg match {
    case line: String => val client = clients.get(context).get
      println("server get message from client " + client.name + ": " + line)
      projects.findForClient(client) match {
        case Some(project) => handleEventInProject(project, parseEvent(line), client)
        case _ => handleEventWithoutProject(parseEvent(line), client)
      }
    case _ => throw new Exception("### unknown msg type: " + msg)
  }

  private def handleWorkingModeRequest(project: Project, newMode: WorkingMode.Value, client: Client) = {
    project.myWorkingMode = newMode
    broadcastServerStatusResponse(Some(client))
  }

  private def handleEventInProject(project: Project, event: PairEvent, client: Client) = event match {
    case CreateProjectRequest(projectName, clientName) => handleCreateProjectRequest(client, projectName, clientName)
    case JoinProjectRequest(projectName, clientName) => handleJoinProjectRequest(client, projectName, clientName)
    case CaretSharingModeRequest => handleWorkingModeRequest(project, WorkingMode.CaretSharing, client)
    case ParallelModeRequest => handleWorkingModeRequest(project, WorkingMode.Parallel, client)
    case event: ChangeMasterEvent => handleChangeMasterEvent(client, event)
    case event: OpenTabEvent => handleOpenTabEvent(client, event)
    case event: CloseTabEvent => broadcastToOtherMembers(client, event)
    case event: ResetTabEvent => handleResetTabEvent(client, event)
    case ResetTabRequest => sendToMaster(client, ResetTabRequest)
    case event: ChangeContentEvent => handleChangeContentEvent(client, event)
    case event: ResetContentEvent => handleResetContentEvent(client, event)
    case event: MoveCaretEvent => broadcastToOtherMembers(client, event)
    case event: IgnoreFilesRequest => handleIgnoreFilesRequest(client, event)
    case SyncFilesRequest => sendToMaster(client, SyncFilesRequest)
    case event: MasterPairableFiles => broadcastToOtherMembers(client, event)
    case _ => broadcastToOtherMembers(client, event)
  }

  private def handleEventWithoutProject(event: PairEvent, client: Client) = {
    event match {
      case CreateProjectRequest(projectName, clientName) => handleCreateProjectRequest(client, projectName, clientName)
      case JoinProjectRequest(projectName, clientName) => handleJoinProjectRequest(client, projectName, clientName)
      case _ => client.writeEvent(AskForJoinProject(Some("You need to join a project first")))
    }
  }

  private def handleResetTabEvent(client: Client, event: ResetTabEvent) {
    projects.findForClient(client).foreach(_.members.foreach(_.projectSpecifiedLocks.activeTabLocks.clear()))
    broadcastToSameProjectMembersThen(client, event)(_.projectSpecifiedLocks.activeTabLocks.add(event.path))
  }

  private def handleResetContentEvent(client: Client, event: ResetContentEvent) {
    for {
      project <- projects.findForClient(client)
      member <- project.members
      lock <- member.pathSpecifiedLocks.get(event.path)
    } lock.contentLocks.clear()
    broadcastToSameProjectMembersThen(client, event)(_.pathSpecifiedLocks.get(event.path).foreach(_.contentLocks.add(event.summary)))
  }

  private def handleIgnoreFilesRequest(client: Client, request: IgnoreFilesRequest) {
    projects.findForClient(client).foreach(_.ignoredFiles = request.files)
    broadcastServerStatusResponse(Some(client))
  }

  private def handleCreateProjectRequest(client: Client, projectName: String, clientName: String) {
    if (projects.contains(projectName)) {
      client.writeEvent(AskForJoinProject(Some(s"Project '$projectName' is already existed")))
    } else {
      projects.findForClient(client) match {
        case Some(p) => p.removeMember(client)
        case _ =>
      }
      projects.create(client, projectName, clientName)
      broadcastServerStatusResponse(Some(client))
    }
  }


  private def handleJoinProjectRequest(client: Client, projectName: String, clientName: String) {
    projects.get(projectName) match {
      case Some(p) => {
        if (p.otherMembers(client).exists(_.name == Some(clientName))) {
          client.writeEvent(AskForJoinProject(Some(s"The client name '$clientName' is already used in project '$projectName'")))
        } else {
          if (p.hasMember(client)) {
            client.name = Some(clientName)
          } else {
            p.addMember(client, clientName)
          }
          broadcastServerStatusResponse(Some(client))
        }
      }
      case _ => client.writeEvent(AskForJoinProject(Some(s"Project '$projectName' is not existed")))
    }
  }

  private def handleChangeContentEvent(client: Client, event: ChangeContentEvent) {
    val locks = client.pathSpecifiedLocks.getOrCreate(event.path).contentLocks
    locks.headOption match {
      case Some(x) if x == event.summary => locks.removeHead()
      case Some(_) => sendToMaster(client, new ResetContentRequest(event.path))
      case _ => broadcastToSameProjectMembersThen(client, event)(_.pathSpecifiedLocks.getOrCreate(event.path).contentLocks.add(event.summary))
    }
  }

  private def handleOpenTabEvent(client: Client, event: OpenTabEvent) {
    val locks = client.projectSpecifiedLocks.activeTabLocks
    locks.headOption match {
      case Some(x) if x == event.path => locks.removeHead()
      case Some(_) => sendToMaster(client, ResetTabRequest)
      case _ => broadcastToSameProjectMembersThen(client, event)(_.projectSpecifiedLocks.activeTabLocks.add(event.path))
    }
  }

  private def broadcastToOtherMembers(client: Client, pairEvent: PairEvent): Unit = broadcastToSameProjectMembersThen(client, pairEvent)(identity)

  private def broadcastToSameProjectMembersThen(client: Client, pairEvent: PairEvent)(f: Client => Any) {
    def otherMembers = projects.findForClient(client).map(_.otherMembers(client)).getOrElse(Nil)
    otherMembers.foreach { otherMember =>
      def doit() {
        otherMember.writeEvent(pairEvent)
        f(otherMember)
      }
      pairEvent match {
        case _: ChangeContentEvent | _: ResetContentEvent |
             _: CreateFileEvent | _: DeleteFileEvent | _: CreateDirEvent | _: DeleteDirEvent | _: RenameEvent => doit()
        case _ if areSharingCaret(client) => doit()
        case _ =>
      }
    }
  }

  private def areSharingCaret(data: Client) = projects.findForClient(data).forall(_.isSharingCaret)

  private def handleChangeMasterEvent(client: Client, event: ChangeMasterEvent) {
    projects.findForClient(client).foreach { project =>
      if (project.hasMember(event.clientName)) {
        project.setMaster(event.clientName)
        broadcastServerStatusResponse(Some(client))
      } else {
        client.writeEvent(ServerErrorResponse(s"Specified user '${event.clientName}' is not found"))
      }
    }
  }

  private def broadcastServerStatusResponse(sourceClient: Option[Client]) {
    sourceClient.foreach(sendClientInfo)

    def clientInfo(project: Project, client: Client) = ClientInfoResponse(projects.findForClient(client).get.name, client.name.get, client.isMaster)
    val event = ServerStatusResponse(
      projects = projects.all.map(p => ProjectInfoData(p.name, p.members.map(clientInfo(p, _)), p.ignoredFiles, p.myWorkingMode)).toList,
      freeClients = clients.size - projects.all.flatMap(_.members).size)
    clients.all.foreach(_.writeEvent(event))
  }

  private def sendClientInfo(client: Client) = {
    projects.findForClient(client).foreach { project =>
      client.writeEvent(ClientInfoResponse(project.name, client.name.get, client.isMaster))
    }
  }

  private def sendToMaster(client: Client, resetEvent: PairEvent) {
    projects.findForClient(client).flatMap(_.getMasterMember).foreach(_.writeEvent(resetEvent))
  }
}
