package com.thoughtworks.pli.intellij.remotepair

import com.thoughtworks.pli.intellij.remotepair.GoogleDiffMatchPatch.Diff

import scala.collection.JavaConversions._

object StringDiff {

  def diffs(original: String, modified: String): Seq[ContentDiff] = {
    val x = new GoogleDiffMatchPatch
    val result = x.diff_main(original, modified)

    convertDiffs(result)
  }

  def applyDiffs(original: String, diffs: Seq[ContentDiff]): String = {
    diffs.foldLeft(original) {
      case (result, Insert(offset, content)) =>
        val (h, t) = result.splitAt(offset)
        h + content + t
      case (result, Delete(offset, length)) =>
        val (h, t) = result.splitAt(offset)
        if (length >= t.length) {
          h
        } else {
          h + t.substring(length)
        }
    }
  }

  def adjustDiffs0(diffs1: Seq[ContentDiff], diffs2: Seq[ContentDiff]): Seq[ContentDiff] = {
    def adjust(diff: ContentDiff) = diffs1.foldLeft(diff) {
      case (op: Insert, Insert(offset, content)) if op.offset >= offset => op.copy(offset = op.offset + content.length)
      case (op: Delete, Insert(offset, content)) if op.offset >= offset => op.copy(offset = op.offset + content.length)
      case (op: Insert, Delete(offset, length)) if op.offset >= offset => op.copy(offset = math.max(offset, op.offset - length))
      case (op: Delete, Delete(offset, length)) if op.offset >= offset => op.copy(offset = math.max(offset, op.offset - length))
      case (finalDiff, _) => finalDiff
    }
    val adjusted2 = diffs2.map(adjust)
    flatOperationsOnSamePosition(adjusted2)
  }

  def adjustDiffs(diffs1: Seq[ContentDiff], diffs2: Seq[ContentDiff]): Seq[ContentDiff] = {
    diffs1 ++: adjustDiffs0(diffs1, diffs2)
  }

  private def flatOperationsOnSamePosition(diffs: Seq[ContentDiff]): Seq[ContentDiff] = {
    diffs.foldLeft(List.empty[ContentDiff]) {
      case (Nil, current) => current :: Nil
      case (list@(prev :: _), current) => (current, prev) match {
        case (op@Insert(offset2, content2), Insert(offset1, content1)) if offset2 <= offset1 + content1.length => op.copy(offset = offset1 + content1.length) :: list
        case (op@Delete(offset2, _), Insert(offset1, content1)) if offset2 <= offset1 + content1.length => op.copy(offset = offset1 + content1.length) :: list
        case (op@Insert(offset2, content2), Delete(offset1, length1)) if offset2 < offset1 => op.copy(offset = offset1) :: list
        case (op@Delete(offset2, _), Delete(offset1, length1)) if offset2 < offset1 => op.copy(offset = offset1) :: list
        case _ => current :: list
      }
    }.reverse
  }

  private def convertDiffs(diffs: java.util.LinkedList[GoogleDiffMatchPatch.Diff]): Seq[ContentDiff] = {
    val diffsWithPosition = diffs.toList.foldLeft(List.empty[(Diff, Int)]) {
      case (Nil, item) => (item, 0) :: Nil
      case (list@((prev, position) :: _), item) =>
        if (prev.operation == GoogleDiffMatchPatch.Operation.DELETE) {
          (item, position) :: list
        } else {
          (item, position + prev.text.length) :: list
        }
    }.reverse

    diffsWithPosition.filter(_._1.operation != GoogleDiffMatchPatch.Operation.EQUAL).map {
      case (diff, position) =>
        if (diff.operation == GoogleDiffMatchPatch.Operation.INSERT)
          Insert(position, diff.text)
        else
          Delete(position, diff.text.length)
    }
  }
}

sealed trait ContentDiff

case class Insert(offset: Int, content: String) extends ContentDiff

case class Delete(offset: Int, length: Int) extends ContentDiff

