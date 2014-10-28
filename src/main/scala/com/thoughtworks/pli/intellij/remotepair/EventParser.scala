package com.thoughtworks.pli.intellij.remotepair

import net.liftweb.json.{Serialization, DefaultFormats}

trait EventParser {

  def parseEvent(line: String): PairEvent = {
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
      case "CaretSharingModeRequest" => Serialization.read[CaretSharingModeRequest](json)
      case "FollowModeRequest" => Serialization.read[FollowModeRequest](json)
      case "CreateProjectRequest" => Serialization.read[CreateProjectRequest](json)
      case "JoinProjectRequest" => Serialization.read[JoinProjectRequest](json)
      case "ParallelModeRequest" => Serialization.read[ParallelModeRequest](json)
      case _ =>
        println("##### unknown line: " + line)
        NoopEvent()
    }
  }

}
