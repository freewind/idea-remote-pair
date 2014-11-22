package com.thoughtworks.pli.intellij.remotepair

import net.liftweb.json.{DefaultFormats, Serialization}

trait PairEvent {
  implicit val formats = DefaultFormats
  def toJson: String

  def toMessage: String = s"$eventName $toJson\n"
  private def eventName: String = getClass.getSimpleName.takeWhile(_ != '$').mkString
}

abstract class LoginEvent extends PairEvent

case class ClientInfoEvent(ip: String, name: String) extends LoginEvent {
  override def toJson = Serialization.write(this)
}

case class ChangeMasterEvent(name: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class ServerStatusResponse(projects: Seq[ProjectInfoData], freeClients: Seq[ClientInfoResponse]) extends PairEvent {
  override def toJson = Serialization.write(this)
  def findProject(name: String) = projects.find(_.name == name)
}

case class ProjectInfoData(name: String, clients: Seq[ClientInfoResponse], ignoredFiles: Seq[String])

case class ClientInfoResponse(project: Option[String] = None, ip: String, name: String, isMaster: Boolean, workingMode: Option[WorkingModeEvent]) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class SyncFilesRequest() extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class IgnoreFilesRequest(files: Seq[String]) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class ServerErrorResponse(message: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

abstract class WorkingModeEvent extends LoginEvent

case object CaretSharingModeRequest extends WorkingModeEvent {
  override def toJson = Serialization.write(this)
}

case object ParallelModeRequest extends WorkingModeEvent {
  override def toJson = Serialization.write(this)
}

case class FollowModeRequest(name: String) extends WorkingModeEvent {
  override def toJson = Serialization.write(this)
}

case class ChangeModeEvent(message: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class RenameEvent(from: String, to: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class CreateDirEvent(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class DeleteDirEvent(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class OpenTabEvent(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class CloseTabEvent(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class ChangeContentEvent(path: String, offset: Int, oldFragment: String, newFragment: String, summary: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class MoveCaretEvent(path: String, offset: Int) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class ResetCaretRequest(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class ResetCaretEvent(path: String, offset: Int) extends PairEvent {
  override def toJson = Serialization.write(this)
}


case class CreateFileEvent(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class DeleteFileEvent(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class SelectContentEvent(path: String, offset: Int, length: Int) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class ResetSelectionRequest(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class ResetSelectionEvent(path: String, offset: Int, length: Int) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class RejectModificationEvent() extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class ResetContentRequest(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class ResetTabRequest() extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class ResetTabEvent(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}


case class ResetContentEvent(path: String, content: String, summary: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class JoinProjectRequest(name: String) extends LoginEvent {
  override def toJson = Serialization.write(this)
}

case class CreateProjectRequest(name: String) extends LoginEvent {
  override def toJson = Serialization.write(this)
}

case class ServerMessageResponse(message: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case object AskForClientInformation extends PairEvent {
  override def toJson = Serialization.write(this)
}

case object AskForJoinProject extends PairEvent {
  override def toJson = Serialization.write(this)
}

case object AskForWorkingMode extends PairEvent {
  override def toJson = Serialization.write(this)
}

