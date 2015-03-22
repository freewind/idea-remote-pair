package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.utils.{NewUuid, StringDiff}

object ClientVersionedDocument {
  type Factory = CreateDocumentConfirmation => ClientVersionedDocument
}

// FIXME refactor the code !!!
class ClientVersionedDocument(creation: CreateDocumentConfirmation)(logger: Logger, publishEvent: PublishEvent, newUuid: NewUuid) {

  case class CalcError(baseVersion: Int, baseContent: String, availableChanges: List[ChangeContentConfirmation], latestVersion: Int, calcContent: String, serverContent: String)

  val path = creation.path
  private var baseVersion: Option[Int] = Some(creation.version)
  private var baseContent: Option[Content] = Some(creation.content)

  private var pendingChange: Option[Change] = None

  private var availableChanges: List[ChangeContentConfirmation] = Nil
  private var backlogChanges: List[ChangeContentConfirmation] = Nil

  def handleContentChange(change: ChangeContentConfirmation, currentContent: String): Option[String] = synchronized {
    determineChange(change)

    if (availableChanges.nonEmpty) {
      handleChanges(currentContent)
    } else {
      None
    }
  }

  def submitContent(content: String): Boolean = synchronized {
    (baseVersion, baseContent) match {
      case (Some(version), Some(Content(text, _))) if text != content =>
        val diffs = StringDiff.diffs(text, content).toList
        if (pendingChange.isEmpty) {
          val eventId = newUuid()
          pendingChange = Some(Change(eventId, version, diffs))
          publishEvent(ChangeContentEvent(eventId, path, version, diffs))
          true
        } else {
          logger.info("##### pendingChange is not empty: " + pendingChange.map(_.eventId))
          false
        }
      case _ => false
    }
  }

  def latestVersion = synchronized(availableChanges.lastOption.map(_.newVersion).orElse(baseVersion))

  def latestContent = synchronized {
    baseContent.map {
      case Content(text, charset) => Content(StringDiff.applyDiffs(text, availableChanges.flatMap(_.diffs)), charset)
    }
  }

  private def determineChange(change: ChangeContentConfirmation) = {
    backlogChanges = change :: backlogChanges
    val available = findSeq(latestVersion.get, backlogChanges)
    availableChanges = availableChanges ::: available
    backlogChanges = backlogChanges.filterNot(available.contains)
  }

  private def handleChanges(currentContent: String): Option[String] = {
    pendingChange match {
      case Some(Change(eventId, pendingBaseVersion, pendingDiffs)) if availableChanges.exists(_.forEventId == eventId) => {
        require(Some(pendingBaseVersion) == baseVersion)

        logger.info("#### received events with id: " + eventId + ", which is the same as the pending one")

        val localTargetContent = baseContent.map(_.text).map { base =>
          val pendingContent = StringDiff.applyDiffs(base, pendingDiffs)
          val localDiffsBasedOnPending = StringDiff.diffs(pendingContent, currentContent)
          val extraDiffs = availableChanges.dropWhile(_.forEventId != eventId).tail.flatMap(_.diffs)
          val adjustedLocalDiffs = StringDiff.adjustLaterDiffs(extraDiffs, localDiffsBasedOnPending)
          val allDiffs = availableChanges.flatMap(_.diffs) ::: adjustedLocalDiffs.toList
          StringDiff.applyDiffs(base, allDiffs)
        }
        logger.info("## pendingChange is gonna be removed: " + pendingChange.map(_.eventId))
        pendingChange = None
        upgradeToNewVersion()
        localTargetContent
      }
      case Some(_) => {
        None
      }
      case _ =>
        val localTargetContent = baseContent.map(_.text).map { base =>
          val allComingDiffs = availableChanges.flatMap(_.diffs)
          val localDiffs = StringDiff.diffs(base, currentContent)
          val adjustedDiffs = StringDiff.adjustAndMergeDiffs(allComingDiffs, localDiffs)
          StringDiff.applyDiffs(base, adjustedDiffs)
        }
        upgradeToNewVersion()
        localTargetContent
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
    logger.info("### base version now is upgraded to: " + baseVersion)
    availableChanges = Nil
  }

}
