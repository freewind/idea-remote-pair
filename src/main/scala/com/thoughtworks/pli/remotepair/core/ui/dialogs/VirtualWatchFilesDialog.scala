package com.thoughtworks.pli.remotepair.core.ui.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.WatchFilesRequest
import com.thoughtworks.pli.remotepair.core.MyUtils
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyFile, MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.{VirtualButton, VirtualFileTree, VirtualList}
import com.thoughtworks.pli.remotepair.core.ui.dialogs.VirtualWatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

import scala.concurrent.ExecutionContext.Implicits.global

object VirtualWatchFilesDialog {
  type ExtraOnCloseHandler = () => Unit
}

trait VirtualWatchFilesDialog extends BaseVirtualDialog {
  def myIde: MyIde
  def myClient: MyClient
  def pairEventListeners: PairEventListeners
  def currentProject: MyProject
  def myUtils: MyUtils

  val okButton: VirtualButton
  val closeButton: VirtualButton
  val watchButton: VirtualButton
  val deWatchButton: VirtualButton
  val workingTree: VirtualFileTree
  val watchingList: VirtualList
  val extraOnCloseHandler: Option[ExtraOnCloseHandler]

  override def init(): Unit = {
    dialog.title = "Choose the files you want to pair with others"

    dialog.onOpen(initUiData(myClient.serverWatchingFiles))
    okButton.onClick(publishWatchFilesRequestToServer())
    closeButton.onClick(dialog.dispose())
    okButton.onClick(extraOnCloseHandler.foreach(_()))
    closeButton.onClick(extraOnCloseHandler.foreach(_()))
    watchButton.onClick(watchSelectedFiles())
    deWatchButton.onClick(deWatchSelectedFiles())
  }

  def initUiData(watchingFiles: Seq[String]): Unit = {
    val simplified = removeDuplicatePaths(watchingFiles)
    initFileTree(workingTree, currentProject.baseDir, !isInPathList(_, simplified))
    initListItems(watchingList, simplified.sorted)
  }

  private def initListItems(watchList: VirtualList, items: Seq[String]): Unit = {
    watchList.items = items
  }

  private def isInPathList(file: MyFile, paths: Seq[String]): Boolean = {
    val relativePath = currentProject.getRelativePath(file.path)
    paths.exists(p => relativePath == Some(p) || relativePath.exists(_.startsWith(p + "/")))
  }


  private def initFileTree(workingTree: VirtualFileTree, baseDir: MyFile, filter: MyFile => Boolean): Unit = {
    workingTree.init(baseDir, filter)
  }

  def removeDuplicatePaths(paths: Seq[String]): Seq[String] = {
    paths.foldLeft(List.empty[String]) {
      case (result, item) => result.headOption match {
        case Some(prev) => if (myUtils.isSubPath(item, prev)) result else item :: result
        case _ => item :: result
      }
    }.reverse
  }

  private def publishWatchFilesRequestToServer() = {
    val future = myClient.publishEvent(WatchFilesRequest(watchingList.items))
//    future.onSuccess { case _ => dialog.dispose() }
//    future.onFailure { case e: Throwable => currentProject.showErrorDialog(message = e.toString) }
  }

  private def watchSelectedFiles() = {
    initUiData(workingTree.selectedFiles ++: watchingList.items)
  }


  private def deWatchSelectedFiles() = {
    val selected = watchingList.selectedItems
    initUiData(watchingList.items.filterNot(selected.contains))
  }

}
