package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.WatchFilesRequest
import com.thoughtworks.pli.remotepair.core.MyUtils
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyFile, MyIde, MyProject}
import com.thoughtworks.pli.remotepair.core.ui.dialogs.BaseVirtualDialog
import com.thoughtworks.pli.remotepair.idea.dialogs.SwingVirtualImplicits._
import com.thoughtworks.pli.remotepair.idea.dialogs.WatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

import scala.concurrent.ExecutionContext.Implicits.global

object WatchFilesDialog {
  type ExtraOnCloseHandler = () => Unit
}

class WatchFilesDialog(val extraOnCloseHandler: Option[ExtraOnCloseHandler])(val myIde: MyIde, val myClient: MyClient, val pairEventListeners: PairEventListeners, val currentProject: MyProject, val myUtils: MyUtils)
  extends _WatchFilesDialog with JDialogSupport with BaseVirtualDialog {

  setSize(Size(600, 400))
  init()

  def init(): Unit = {
    this.title = "Choose the files you want to pair with others"

    this.onOpen(initUiData(myClient.serverWatchingFiles))
    _okButton.onClick(publishWatchFilesRequestToServer())
    _closeButton.onClick(this.dispose())
    _okButton.onClick(extraOnCloseHandler.foreach(_ ()))
    _closeButton.onClick(extraOnCloseHandler.foreach(_ ()))
    _watchButton.onClick(watchSelectedFiles())
    _deWatchButton.onClick(deWatchSelectedFiles())
  }

  def initUiData(watchingFiles: Seq[String]): Unit = {
    val simplified = removeDuplicatePaths(watchingFiles)
    initFileTree(_workingTree, currentProject.baseDir, !isInPathList(_, simplified))
    initListItems(_watchingList, simplified.sorted)
  }

  private def initListItems(watchList: RichList, items: Seq[String]): Unit = {
    watchList.items = items
  }

  private def isInPathList(file: MyFile, paths: Seq[String]): Boolean = {
    val relativePath = currentProject.getRelativePath(file.path)
    paths.exists(p => relativePath == Some(p) || relativePath.exists(_.startsWith(p + "/")))
  }


  private def initFileTree(workingTree: RichFileTree, baseDir: MyFile, filter: MyFile => Boolean): Unit = {
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
    val future = myClient.publishEvent(WatchFilesRequest(_watchingList.items))
    future.onSuccess { case _ => this.dispose() }
    future.onFailure { case e: Throwable => currentProject.showErrorDialog(message = e.toString) }
  }

  private def watchSelectedFiles() = {
    initUiData(_workingTree.selectedFiles ++: _watchingList.items)
  }


  private def deWatchSelectedFiles() = {
    val selected = _watchingList.selectedItems
    initUiData(_watchingList.items.filterNot(selected.contains))
  }


}
