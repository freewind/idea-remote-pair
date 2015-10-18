package com.thoughtworks.pli.remotepair.core.server_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.utils.{ContentDiff, StringDiff}
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.{MySystem, MyUtils, PluginLogger}

object ClientVersionedDocument {
  type Factory = CreateDocumentConfirmation => ClientVersionedDocument

  sealed trait SubmitContentResult
  sealed trait RemoteChangeResult

  case object Timeout extends SubmitContentResult with RemoteChangeResult
  case object Published extends SubmitContentResult
  case object Pending extends SubmitContentResult

  case class LocalContentChanged(text: String) extends RemoteChangeResult
  case object LocalContentNoChange extends RemoteChangeResult

  case class Change(eventId: String, baseVersion: Int, diffs: Seq[ContentDiff])
  case class InflightChange(change: Change, timestamp: Long)
}


// FIXME refactor the code !!!
class ClientVersionedDocument(creation: CreateDocumentConfirmation)(logger: PluginLogger, myClient: MyClient, myUtils: MyUtils, mySystem: MySystem) {

  import ClientVersionedDocument._

  val path = creation.path
  private var _baseVersion: Int = creation.version
  private var _baseContent: Content = creation.content
  private var inflightChange: Option[InflightChange] = None
  private var pendingChange: Option[PendingChange] = None

  def baseVersion: Int = synchronized(_baseVersion)
  def baseContent: Content = synchronized(_baseContent)

  def handleContentChange(serverChange: ChangeContentConfirmation, getLocalContent: () => String, callback: RemoteChangeResult => Unit): Unit = synchronized {
    require(serverChange.newVersion == _baseVersion + 1, s"serverChange.newVersion(${serverChange.newVersion}) == baseVersion(${_baseVersion}) + 1")

    inflightChange match {
      case Some(change) if isTimeout(change) => callback(Timeout)
      case Some(InflightChange(Change(eventId, inflightBaseVersion, _), _)) if serverChange.forEventId == eventId => {
        info(s"received events with for inflight changed id: $eventId, will clear inflight change")

        _baseContent = _baseContent.copy(text = StringDiff.applyDiffs(_baseContent.text, serverChange.diffs))
        _baseVersion = serverChange.newVersion
        inflightChange = None
        pendingChange.foreach { case PendingChange(getDocContent, pendingCallback) => submitContent(getDocContent, pendingCallback) }
        callback(LocalContentNoChange)
      }
      case _ =>
        val adjustedLocalDiffs = StringDiff.adjustAndMergeDiffs(serverChange.diffs, StringDiff.diffs(_baseContent.text, getLocalContent()))
        val localTargetContent = StringDiff.applyDiffs(_baseContent.text, adjustedLocalDiffs)

        _baseContent = _baseContent.copy(text = StringDiff.applyDiffs(_baseContent.text, serverChange.diffs))
        _baseVersion = serverChange.newVersion
        callback(LocalContentChanged(localTargetContent))
    }
  }

  case class PendingChange(getDocContent: () => String, callback: Boolean => Unit)

  def submitContent(getDocContent: () => String, callback: Boolean => Unit): Unit = synchronized {
    inflightChange match {
      case Some(change) if isTimeout(change) => callback(false)
      case Some(_) =>
        info(s"inflightChange is not empty: $inflightChange, store this intention")
        pendingChange = Some(PendingChange(getDocContent, callback))
      case None =>
        val content = getDocContent()
        (_baseVersion, _baseContent) match {
          case (version, Content(text, _)) if text != content =>
            val diffs = StringDiff.diffs(text, content).toList
            val eventId = myUtils.newUuid()
            inflightChange = Some(InflightChange(Change(eventId, version, diffs), mySystem.now))
            myClient.publishEvent(ChangeContentEvent(eventId, path, version, diffs))
            callback(true)
          case _ =>
        }

    }
  }

  private def isTimeout(change: InflightChange) = mySystem.now - change.timestamp > 2000

  private def findSeq(baseNum: Int, numbers: List[ChangeContentConfirmation]): List[ChangeContentConfirmation] = {
    numbers.filter(_.newVersion > baseNum).sortBy(_.newVersion).foldLeft(List.empty[ChangeContentConfirmation]) {
      case (result, item) => result match {
        case Nil if item.newVersion == baseNum + 1 => item :: result
        case Nil => result
        case h :: _ => if (item.newVersion == h.newVersion + 1) item :: result else result
      }
    }.reverse
  }

  override def toString: String = {
    s"""
       |ClientVersionedDocument {
       |  path: $path,
       |  baseVersion: ${_baseVersion},
       |  baseContent: ${_baseContent},
       |  inflightChange: $inflightChange,
       |}""".stripMargin
  }

  private def info(message: String): Unit = {
    logger.info(s"($path) $message")
  }
}

