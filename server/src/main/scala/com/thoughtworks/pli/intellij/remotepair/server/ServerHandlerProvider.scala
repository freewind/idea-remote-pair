package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel._
import com.thoughtworks.pli.intellij.remotepair._
import scala.Some
import com.thoughtworks.pli.intellij.remotepair.ClientInfoEvent

class ServerHandlerProvider extends ChannelHandlerAdapter with EventParser {
  val contexts: Contexts = Contexts
  val projects: Projects = Projects

  override def channelActive(ctx: ChannelHandlerContext) {
    contexts.add(ctx)
    contexts.get(ctx).foreach(askForLogin)
    broadcastServerStatusResponse()
  }

  private def askForLogin(data: ContextData) {
    sendClientInfo(data)

    if (!data.hasUserInformation) {
      data.writeEvent(AskForClientInformation)
    } else {
      projects.findForClient(data) match {
        case None => data.writeEvent(AskForJoinProject)
        case Some(p) if data.myWorkingMode.isEmpty => data.writeEvent(AskForWorkingMode)
        case _ => broadcastServerStatusResponse()
      }
    }
  }

  private def sendClientInfo(data: ContextData) = {
    data.writeEvent(ClientInfoResponse(projects.findForClient(data).map(_.name), data.ip, data.name, data.master))
  }

  override def channelInactive(ctx: ChannelHandlerContext) {
    contexts.get(ctx).foreach { d =>
      project(d).foreach(_.removeMember(d))
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
            case req: WorkingModeEvent => if (projects.findForClient(data).isDefined) {
              req match {
                case CaretSharingModeRequest => handleCaretSharingModeRequest(data)
                case ParallelModeRequest => handleParallelModeRequest(data)
              }
            } else {
              data.writeEvent(ServerErrorResponse("Operation is not allowed because you have not joined in any project"))
            }
          }
          askForLogin(data)
        }

        case others => if (projects.findForClient(data).isDefined) {
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

            case SyncFilesRequest => handleSyncFilesRequest()
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
    projects.findForClient(data).foreach { project =>
      project.ignoredFiles = request.files
    }
    broadcastServerStatusResponse()
  }

  def handleCaretSharingModeRequest(context: ContextData) {
    if (projects.findForClient(context).isEmpty) {
      context.writeEvent(ServerErrorResponse("Operation is not allowed because you have not joined in any project"))
    } else {
      context.myWorkingMode = Some(CaretSharingModeRequest)
    }
  }

  private def project(context: ContextData) = projects.findForClient(context)

  private def notAStar(data: ContextData) {
    for {
      p <- project(data)
      member <- p.members
    } member.myWorkingMode = None
  }

  def handleParallelModeRequest(data: ContextData) {
    data.myWorkingMode = Some(ParallelModeRequest)
  }

  def handleCreateProjectRequest(data: ContextData, request: CreateProjectRequest) {
    if (data.hasUserInformation) {
      val projectName = request.name
      if (projects.contains(projectName)) {
        data.writeEvent(ServerErrorResponse(s"Project '$projectName' is already exist, can't create again"))
      } else {
        createOrJoinProject(projectName, data)
      }
    } else {
      data.writeEvent(ServerErrorResponse("Please tell me your information first"))
    }
  }

  private def createOrJoinProject(projectName: String, userName: ContextData) {
    projects.createOrJoin(projectName, userName)

    projects.singles.map(_.members.head).foreach(_.master = true)
    broadcastServerStatusResponse()
  }

  def handleJoinProjectRequest(data: ContextData, request: JoinProjectRequest) {
    if (data.hasUserInformation) {
      val projectName = request.name
      if (!projects.contains(projectName)) {
        data.writeEvent(ServerErrorResponse(s"You can't join a non-existent project: '$projectName'"))
      } else {
        createOrJoinProject(projectName, data)
      }
    } else {
      data.writeEvent(ServerErrorResponse("Please tell me your information first"))
    }

  }

  def handleSyncFilesRequest() {
    sendToMaster(SyncFilesRequest)
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
      case Some(_) => sendToMaster(ResetTabRequest)
      case _ => broadcastToSameProjectMembersThen(data, event)(_.projectSpecifiedLocks.activeTabLocks.add(event.path))
    }
  }

  def handleCloseTabEvent(data: ContextData, event: CloseTabEvent) {
    broadcastToSameProjectMembersThen(data, event)(identity)
  }

  private def broadcastToSameProjectMembersThen(data: ContextData, pairEvent: PairEvent)(f: ContextData => Any) {
    def projectMembers = projects.findForClient(data).fold(Seq.empty[ContextData])(_.members)
    projectMembers.filter(_.context != data.context).foreach { otherData =>
      def doit() {
        otherData.writeEvent(pairEvent)
        f(otherData)
      }

      pairEvent match {
        case _: ChangeContentEvent | _: ResetContentEvent => doit()
        case _: CreateFileEvent | _: DeleteFileEvent | _: CreateDirEvent | _: DeleteDirEvent | _: RenameEvent => doit()
        case x if areSharingCaret(data, otherData) => doit()
        case _ =>
      }
    }

  }

  private def areSharingCaret(data: ContextData*) = data.forall(_.isSharingCaret)

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
    def client2data(d: ContextData) = ClientInfoResponse(projects.findForClient(d).map(_.name), d.ip, d.name, d.master)
    val ps = projects.all.map(p => ProjectInfoData(p.name, p.members.map(client2data), p.ignoredFiles)).toList
    val freeClients = contexts.all.filter(c => projects.findForClient(c).isEmpty).map(client2data)
    val event = ServerStatusResponse(ps, freeClients)
    contexts.all.foreach(_.writeEvent(event))
  }

  def sendToMaster(resetEvent: PairEvent) {
    contexts.all.find(_.master).foreach(_.writeEvent(resetEvent))
  }
}
