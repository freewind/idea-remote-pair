package com.thoughtworks.pli.intellij.remotepair

import net.liftweb.json.Serialization

sealed trait PairEvent {
  def toJson: String

  def toMessage: String = s"${getClass.getSimpleName} $toJson"
}

case class NewClientEvent(ip: String, name: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class BeMasterEvent() extends PairEvent {
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

case class ContentChangeEvent(path: String, offset: Int, oldFragment: String, newFragment: String, summary: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class MoveCaretEvent() extends PairEvent {
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

case class RejectModificationEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class NoopEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ContentResetRequestEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class TabResetRequestEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetTabEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}


case class ResetContentEvent(path: String, content: String, summary: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}