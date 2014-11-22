package com.thoughtworks.pli.intellij.remotepair

import net.liftweb.json.{DefaultFormats, Serialization}

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
      case "CaretSharingModeRequest" => CaretSharingModeRequest
      case "FollowModeRequest" => Serialization.read[FollowModeRequest](json)
      case "CreateProjectRequest" => Serialization.read[CreateProjectRequest](json)
      case "JoinProjectRequest" => Serialization.read[JoinProjectRequest](json)
      case "ParallelModeRequest" => ParallelModeRequest
      case "AskForClientInformation" => AskForClientInformation
      case "AskForJoinProject" => AskForJoinProject
      case "AskForWorkingMode" => AskForWorkingMode
      case "ServerStatusResponse" => Serialization.read[ServerStatusResponse](json)
      case "ClientInfoResponse" => Serialization.read[ClientInfoResponse](json)
      case "ServerErrorResponse" => Serialization.read[ServerErrorResponse](json)
      case "ResetContentRequest" => Serialization.read[ResetContentRequest](json)
      case "ResetCaretRequest" => Serialization.read[ResetCaretRequest](json)
      case "ResetTabRequest" => Serialization.read[ResetTabRequest](json)
      case "ResetSelectionRequest" => Serialization.read[ResetSelectionRequest](json)
      case _ =>
        println("!!!!!!!!!!!!!!!!!!!!! unknown line from server: " + line)
        ???
    }
  }

}