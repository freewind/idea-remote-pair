package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.WatchFilesRequest
import com.thoughtworks.pli.remotepair.core.MyUtils
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyFile, MyIde}
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._
import com.thoughtworks.pli.remotepair.idea.dialogs.WatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

import scala.concurrent.ExecutionContext.Implicits.global

object WatchFilesDialog {
  type ExtraOnCloseHandler = () => Unit
  type Factory = Option[ExtraOnCloseHandler] => WatchFilesDialog
}

trait MyWatchFilesDialog extends MyWindow {
  def myIde: MyIde
  def myClient: MyClient
  def pairEventListeners: PairEventListeners
  def currentProject: IdeaProjectImpl
  def myUtils: MyUtils

  val okButton: VirtualButton
  val closeButton: VirtualButton
  val watchButton: VirtualButton
  val deWatchButton: VirtualButton
  val workingTree: VirtualFileTree
  val watchingList: VirtualList

  dialog.title = "Choose the files you want to pair with others"

  dialog.onOpen(init(myClient.serverWatchingFiles))
  okButton.onClick(publishWatchFilesRequestToServer())
  closeButton.onClick(dialog.dispose())
  //  okButton.onClick(extraOnCloseHandler.foreach(_()))
  //  closeButton.onClick(extraOnCloseHandler.foreach(_()))
  watchButton.onClick(watchSelectedFiles())
  deWatchButton.onClick(deWatchSelectedFiles())

  def init(watchingFiles: Seq[String]): Unit = {
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
    future.onSuccess { case _ => dialog.dispose() }
    future.onFailure { case e: Throwable => currentProject.showErrorDialog(message = e.toString) }
  }

  private def watchSelectedFiles() = {
    init(workingTree.selectedFiles ++: watchingList.items)
  }


  private def deWatchSelectedFiles() = {
    val selected = watchingList.selectedItems
    init(watchingList.items.filterNot(selected.contains))
  }

}

class WatchFilesDialog(extraOnCloseHandler: Option[ExtraOnCloseHandler])(val myIde: MyIde, val myClient: MyClient, val pairEventListeners: PairEventListeners, val currentProject: IdeaProjectImpl, val myUtils: MyUtils)
  extends _WatchFilesDialog with JDialogSupport with MyWatchFilesDialog {

  import SwingVirtualImplicits._

  override val okButton: VirtualButton = _okButton
  override val closeButton: VirtualButton = _closeButton
  override val watchButton: VirtualButton = _watchButton
  override val deWatchButton: VirtualButton = _deWatchButton
  override val workingTree: VirtualFileTree = _workingTree
  override val watchingList: VirtualList = _watchingList
  override val dialog: VirtualDialog = this

  setSize(Size(600, 400))

}
