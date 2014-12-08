package com.thoughtworks.pli.intellij.remotepair

import org.json4s.native.Serialization
import JsonFormats.formats

trait EventParser {

  def parseEvent(line: String): PairEvent = {
    val (name, json) = line.span(_ != ' ')
    name match {
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
      case "SelectContentEvent" => Serialization.read[SelectContentEvent](json)
      case "IgnoreFilesRequest" => Serialization.read[IgnoreFilesRequest](json)
      case "SyncFilesRequest" => Serialization.read[SyncFilesRequest](json)
      case "CaretSharingModeRequest" => CaretSharingModeRequest
      case "CreateProjectRequest" => Serialization.read[CreateProjectRequest](json)
      case "JoinProjectRequest" => Serialization.read[JoinProjectRequest](json)
      case "ParallelModeRequest" => ParallelModeRequest
      case "AskForJoinProject" => Serialization.read[AskForJoinProject](json)
      case "ServerStatusResponse" => Serialization.read[ServerStatusResponse](json)
      case "ClientInfoResponse" => Serialization.read[ClientInfoResponse](json)
      case "ServerErrorResponse" => Serialization.read[ServerErrorResponse](json)
      case "ResetContentRequest" => Serialization.read[ResetContentRequest](json)
      case "ResetTabRequest" => ResetTabRequest
      case "SyncFileEvent" => Serialization.read[SyncFileEvent](json)
      case "MasterPairableFiles" => Serialization.read[MasterPairableFiles](json)
      case _ =>
        println("!!!!!!!!!!!!!!!!!!!!! unknown line from server: " + line)
        ???
    }
  }

}
