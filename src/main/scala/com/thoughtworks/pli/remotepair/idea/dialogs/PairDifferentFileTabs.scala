package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.FileSummary

import scala.collection.JavaConverters._

class PairDifferentFileTabs extends _PairDifferentFileTabs {

  case class PairInfo(pairName: String, pane: _DifferentFilesTabPane, totalCount: Option[Int] = None, completed: Int = 0)

  @volatile private var mapping = Map.empty[String, PairInfo]

  def addTab(pairName: String, basedFiles: Seq[FileSummary], targetFiles: Seq[FileSummary]): Unit = {
    val tabPane = new _DifferentFilesTabPane()
    mapping += pairName -> new PairInfo(pairName, tabPane)
    tabs.addTab(pairName, tabPane.getMainPanel)
    tabPane.setFiles(merge(basedFiles.toSet, targetFiles.toSet).asJava)
  }

  def setTotalCount(pairName: String, count: Int): Unit = mapping.get(pairName).foreach { info =>
    mapping += pairName -> info.copy(totalCount = Some(count))
    updateMessage(pairName)
  }

  def increase(pairName: String): Unit = mapping.get(pairName).foreach { info =>
    mapping += pairName -> info.copy(completed = info.completed + 1)
    updateMessage(pairName)
  }

  def setMessage(pairName: String, message: String) = mapping.get(pairName).foreach { info =>
    info.pane.messageLabel.setText(message)
  }

  private def merge(baseFiles: Set[FileSummary], targetFiles: Set[FileSummary]): Seq[String] = {
    def sort(paths: Set[String]) = paths.toSeq.sorted
    val basePaths = baseFiles.map(_.path)
    val targetPaths = targetFiles.map(_.path)
    val willDelete = basePaths -- targetPaths
    val willAdd = targetPaths -- basePaths
    val changed = (basePaths & targetPaths) -- (baseFiles & targetFiles).map(_.path)

    sort(willAdd) ++ sort(willDelete) ++ sort(changed)
  }

  private def updateMessage(pairName: String) = mapping.get(pairName).foreach {
    case PairInfo(_, pane, Some(total), completed) =>
      val message = s"""Synchronizing files: $completed / $total ${if (total == completed) " Complete!" else ""}"""
      pane.messageLabel.setText(message)
  }

}
