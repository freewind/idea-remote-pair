package com.thoughtworks.pli.remotepair.actors

import akka.actor.Actor
import com.thoughtworks.pli.remotepair.actors.ClientDocumentsActor.{CreateDocument, EditorContentChanged, ServerContentChanged}

class ClientDocumentsActor extends Actor {
  override def receive: Receive = {
    case CreateDocument(path, versionAndContent) => ???
    case EditorContentChanged(path, currentVersionAndContent) => ???
    case ServerContentChanged(path) => ???
  }
}

object ClientDocumentsActor {
  case class CreateDocument(path: String, versionAndContent: VersionAndContent)
  case class EditorContentChanged(path: String, f: () => VersionAndContent)
  case class ServerContentChanged(path: String)

  case class VersionAndContent(version: Int, content: String)
}
