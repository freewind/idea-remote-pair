package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.DefaultListModel

import com.thoughtworks.pli.intellij.remotepair.protocol.FileSummary
import com.thoughtworks.pli.remotepair.idea.dialogs.SwingVirtualImplicits._

import scala.language.reflectiveCalls

class PairDifferentFileTabs extends _PairDifferentFileTabs {
  def createTabPane() = new DifferentFilesTabPanel()
  def addTabPanel(pairName: String, tabPane: DifferentFilesTabPanel): Unit = {
    tabPane match {
      case pane: DifferentFilesTabPanel => _tabs.addTab(pairName, pane.getMainPanel)
      case _ => ???
    }
  }

  case class PairInfo(pairName: String, pane: DifferentFilesTabPanel, totalCount: Option[Int] = None, completed: Int = 0)

  @volatile private var mapping = Map.empty[String, PairInfo]

  def addTab(pairName: String, basedFiles: Seq[FileSummary], targetFiles: Seq[FileSummary]): Unit = {
    val tabPane = createTabPane()
    mapping += pairName -> new PairInfo(pairName, tabPane)
    addTabPanel(pairName, tabPane)
    tabPane.setFiles(merge(basedFiles.toSet, targetFiles.toSet))
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
    info.pane.messageLabel.text_=(message)
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
      pane.messageLabel.text_=(message)
  }

}

class DifferentFilesTabPanel extends _DifferentFilesTabPane {

  val messageLabel: RichLabel = _messageLabel
  def setFiles(files: Seq[String]): Unit = {
    val model = new DefaultListModel
    for (file <- files) {
      model.addElement(file)
    }
    filesList.setModel(model)
  }
}
