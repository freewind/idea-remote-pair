package com.thoughtworks.pli.intellij.remotepair

import net.liftweb.json.Serialization

sealed trait PairEvent {
  def toJson: String

  def toMessage: String = s"${getClass.getSimpleName} $toJson\n"
}

case class ClientInfoEvent(ip: String, name: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

// FIXME name:String
case class ChangeMasterEvent(name: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ServerStatusResponse() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class SyncFilesRequest() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class IgnoreFilesRequest() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ServerErrorResponse(message: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class SyncModeRequest() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ParallelModeRequest() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class RenameEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class CreateDirEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class DeleteDirEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class OpenTabEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class CloseTabEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class LeaveTabEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ChangeContentEvent(path: String, offset: Int, oldFragment: String, newFragment: String, summary: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class MoveCaretEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetCaretRequest() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetCaretEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}


case class CreateFileEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class DeleteFileEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ScrollingEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class SelectContentEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetSelectionRequest() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetSelectionEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class RejectModificationEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class NoopEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetContentRequest(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetTabRequest() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetTabEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}


case class ResetContentEvent(path: String, content: String, summary: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}