package com.thoughtworks.pli.intellij.remotepair.server

import com.thoughtworks.pli.intellij.remotepair._
import io.netty.channel._

object ServerHandlerProvider {
  val clients: Clients = Clients
  val projects: Projects = Projects
}

class ServerHandlerProvider extends ChannelHandlerAdapter with EventParser {

  def clients: Clients = ServerHandlerProvider.clients
  def projects: Projects = ServerHandlerProvider.projects

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

      if (parseEvent(line).getClass.getName.toLowerCase.contains("content")) {
        ServerLogger.info("\n")
        ServerLogger.info("server get message from client " + client.name + ": " + line)
        ServerLogger.info("\n")
      }

      //      println("server get message from client " + client.name + ": " + line)
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

  def handleCreateServerDocumentRequest(client: Client, request: CreateServerDocumentRequest) = {
    sendToMaster(client, request)
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
    case req: SyncFilesRequest => sendToMaster(client, req)
    case event: MasterPairableFiles => broadcastToOtherMembers(client, event)
    case event: CreateDocument => handleCreateDocument(project, client, event)
    case request: CreateServerDocumentRequest => handleCreateServerDocumentRequest(client, request)
    case _ => broadcastToOtherMembers(client, event)
  }

  private def handleCreateDocument(project: Project, client: Client, event: CreateDocument): Unit = {
    val doc = project.documents.find(event.path).fold(project.documents.create(event))(identity)
    broadcastToAllMembers(client, CreateDocumentConfirmation(doc.path, doc.latestVersion, doc.initContent))
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
    broadcastToOtherMembers(client, event)
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
          client.writeEvent(JoinedToProjectEvent(projectName, clientName))
          broadcastServerStatusResponse(Some(client))
        }
      }
      case _ => client.writeEvent(AskForJoinProject(Some(s"Project '$projectName' is not existed")))
    }
  }

  private def handleChangeContentEvent(client: Client, event: ChangeContentEvent) {
    projects.findForClient(client) match {
      case Some(project) => project.documents.find(event.path) match {
        case Some(doc) => doc.synchronized {
          val changes = doc.getLaterChangesFromVersion(event.baseVersion)
          val adjustedChanges = StringDiff.adjustDiffs(changes, event.changes)
          val newDoc = project.documents.update(doc, adjustedChanges)
          val confirm = ChangeContentConfirmation(event.eventId, event.path, newDoc.latestVersion, newDoc.latestChanges, newDoc.latestContent)

          for {
            project <- projects.findForClient(client)
            member <- project.members
          } member.writeEvent(confirm)
        }
        case _ => ???
      }
      case _ =>
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

  private def broadcastToAllMembers(client: Client, pairEvent: PairEvent): Unit = {
    projects.findForClient(client).map(_.members).foreach { members =>
      members.foreach(m => m.writeEvent(pairEvent))
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

    def clientInfo(project: Project, client: Client) = ClientInfoResponse(client.id, projects.findForClient(client).get.name, client.name.get, client.isMaster)
    val event = ServerStatusResponse(
      projects = projects.all.map(p => ProjectInfoData(p.name, p.members.map(clientInfo(p, _)), p.ignoredFiles, p.myWorkingMode)).toList,
      freeClients = clients.size - projects.all.flatMap(_.members).size)
    clients.all.foreach(_.writeEvent(event))
  }

  private def sendClientInfo(client: Client) = {
    projects.findForClient(client).foreach { project =>
      client.writeEvent(ClientInfoResponse(client.id, project.name, client.name.get, client.isMaster))
    }
  }

  private def sendToMaster(client: Client, resetEvent: PairEvent) {
    projects.findForClient(client).flatMap(_.getMasterMember).foreach(_.writeEvent(resetEvent))
  }
}
