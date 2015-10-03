package com.thoughtworks.pli.remotepair.core.server_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.utils.{ContentDiff, StringDiff}
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.{MySystem, MyUtils, PluginLogger}

import scala.util.{Failure, Success, Try}

object ClientVersionedDocument {
  type Factory = CreateDocumentConfirmation => ClientVersionedDocument
}

case class Change(eventId: String, baseVersion: Int, diffs: Seq[ContentDiff])

class InflightChangeTimeoutException(pendingChange: InflightChange) extends Exception

case class InflightChange(change: Change, timestamp: Long)

// FIXME refactor the code !!!
class ClientVersionedDocument(creation: CreateDocumentConfirmation)(logger: PluginLogger, connectedProjectInfo: MyClient, myUtils: MyUtils, mySystem: MySystem) {

  case class CalcError(baseVersion: Int, baseContent: String, availableChanges: List[ChangeContentConfirmation], latestVersion: Int, calcContent: String, serverContent: String)


  val path = creation.path
  private var baseVersion: Option[Int] = Some(creation.version)
  private var baseContent: Option[Content] = Some(creation.content)

  private var inflightChange: Option[InflightChange] = None

  private var availableChanges: List[ChangeContentConfirmation] = Nil
  private var backlogChanges: List[ChangeContentConfirmation] = Nil

  def handleContentChange(serverChange: ChangeContentConfirmation, currentContent: String): Try[Option[String]] = synchronized {
    inflightChange match {
      case Some(change) if isTimeout(change) => Failure(new InflightChangeTimeoutException(change))
      case _ =>
        determineChange(serverChange)

        if (availableChanges.nonEmpty) {
          handleChanges(currentContent)
        } else {
          Success(None)
        }
    }
  }

  def submitContent(content: String): Try[Boolean] = synchronized {
    inflightChange match {
      case Some(pendingChange) if isTimeout(pendingChange) => Failure(new InflightChangeTimeoutException(pendingChange))
      case Some(_) =>
        info(s"pendingChange is not empty: $inflightChange")
        Success(false)
      case None =>
        (baseVersion, baseContent) match {
          case (Some(version), Some(Content(text, _))) if text != content =>
            val diffs = StringDiff.diffs(text, content).toList
            val eventId = myUtils.newUuid()
            inflightChange = Some(InflightChange(Change(eventId, version, diffs), mySystem.now))
            connectedProjectInfo.publishEvent(ChangeContentEvent(eventId, path, version, diffs))
            Success(true)
          case _ => Success(false)
        }
    }
  }

  def latestVersion = synchronized(availableChanges.lastOption.map(_.newVersion).orElse(baseVersion))

  def latestContent = synchronized {
    baseContent.map {
      case Content(text, charset) => Content(StringDiff.applyDiffs(text, availableChanges.flatMap(_.diffs)), charset)
    }
  }

  private def isTimeout(change: InflightChange) = mySystem.now - change.timestamp > 2000

  private def determineChange(change: ChangeContentConfirmation) = {
    backlogChanges = change :: backlogChanges
    val available = findSeq(latestVersion.get, backlogChanges)
    availableChanges = availableChanges ::: available
    backlogChanges = backlogChanges.filterNot(available.contains)
  }

  private def handleChanges(currentContent: String): Try[Option[String]] = {
    inflightChange match {
      case Some(InflightChange(Change(eventId, pendingBaseVersion, pendingDiffs), _)) if availableChanges.exists(_.forEventId == eventId) => {
        require(baseVersion.contains(pendingBaseVersion))

        info(s"received events with id: $eventId, which is the same as the pending one")

        val localTargetContent = baseContent.map(_.text).map { base =>
          val pendingContent = StringDiff.applyDiffs(base, pendingDiffs)
          val localDiffsBasedOnPending = StringDiff.diffs(pendingContent, currentContent)
          val extraDiffs = availableChanges.dropWhile(_.forEventId != eventId).tail.flatMap(_.diffs)
          val adjustedLocalDiffs = StringDiff.adjustLaterDiffs(extraDiffs, localDiffsBasedOnPending)
          val allDiffs = availableChanges.flatMap(_.diffs) ::: adjustedLocalDiffs.toList
          StringDiff.applyDiffs(base, allDiffs)
        }
        info(s"pendingChange is gonna be removed: $inflightChange")
        inflightChange = None
        upgradeToNewVersion()
        Success(localTargetContent)
      }
      case Some(_) => {
        Success(None)
      }
      case _ =>
        val localTargetContent = baseContent.map(_.text).map { base =>
          val allComingDiffs = availableChanges.flatMap(_.diffs)
          val localDiffs = StringDiff.diffs(base, currentContent)
          val adjustedDiffs = StringDiff.adjustAndMergeDiffs(allComingDiffs, localDiffs)
          StringDiff.applyDiffs(base, adjustedDiffs)
        }
        upgradeToNewVersion()
        Success(localTargetContent)
    }

  }

  private def findSeq(baseNum: Int, numbers: List[ChangeContentConfirmation]): List[ChangeContentConfirmation] = {
    numbers.filter(_.newVersion > baseNum).sortBy(_.newVersion).foldLeft(List.empty[ChangeContentConfirmation]) {
      case (result, item) => result match {
        case Nil if item.newVersion == baseNum + 1 => item :: result
        case Nil => result
        case h :: _ => if (item.newVersion == h.newVersion + 1) item :: result else result
      }
    }.reverse
  }

  private def upgradeToNewVersion() {
    baseVersion = latestVersion
    baseContent = latestContent
    info(s"base version now is upgraded to: $baseVersion")
    availableChanges = Nil
  }

  override def toString: String = {
    s"""
    |ClientVersionedDocument {
    |  path: $path,
    |  baseVersion: $baseVersion,
    |  baseContent: $baseContent,
    |  latestVersion: $latestVersion,
    |  latestContent: $latestContent,
    |  changeWaitsForConfirmation: $inflightChange,
    |}""".stripMargin
  }

  private def info(message: String): Unit = {
    logger.info(s"($path) $message")
  }
}
