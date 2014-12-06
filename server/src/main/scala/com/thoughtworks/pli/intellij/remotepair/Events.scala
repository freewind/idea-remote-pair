package com.thoughtworks.pli.intellij.remotepair

import org.json4s.native.Serialization
import JsonFormats.formats

trait PairEvent {
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

case class DDD(i: Int) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ProjectInfoData(name: String, clients: Seq[ClientInfoResponse], ignoredFiles: Seq[String], workingMode: WorkingMode.Value) {
  def isCaretSharing = workingMode == WorkingMode.CaretSharing
}

object WorkingMode extends Enumeration {
  val CaretSharing, Parallel = Value
}

case class ClientInfoResponse(project: Option[String] = None, ip: String, name: String, isMaster: Boolean) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case object SyncFilesRequest extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class MasterPairableFiles(paths: Seq[String]) extends PairEvent {
  // TODO: remove it later
  val invalid = paths.filter(_.startsWith("/Users"))
  if (invalid.nonEmpty) {
    println("!!!!!!!!!!!!!!!! Found invalid paths:")
    invalid.foreach(println)
    throw new RuntimeException("!!!!!!!!!!! Found invalid paths")
  }
  override def toJson = Serialization.write(this)
}

case class SyncFileEvent(path: String, content: Content) extends PairEvent {
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

case class CreateFileEvent(path: String, content: Content) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class DeleteFileEvent(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class SelectContentEvent(path: String, offset: Int, length: Int) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case object RejectModificationEvent extends PairEvent {
  override def toJson = Serialization.write(this)
}

case class ResetContentRequest(path: String) extends PairEvent {
  override def toJson = Serialization.write(this)
}

case object ResetTabRequest extends PairEvent {
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

case class Content(text: String, charset: String)
