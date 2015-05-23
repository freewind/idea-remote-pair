package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.WatchFilesRequest
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyFile, MyIde}
import com.thoughtworks.pli.remotepair.idea.dialogs.WatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.idea.dialogs.list.{GetListItems, InitListItems}
import com.thoughtworks.pli.remotepair.idea.dialogs.utils.{GetSelectedFromFileTree, InitFileTree}
import com.thoughtworks.pli.remotepair.idea.idea.ShowErrorDialog
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

import scala.concurrent.ExecutionContext.Implicits.global

object WatchFilesDialog {
  type ExtraOnCloseHandler = () => Unit
  type Factory = Option[ExtraOnCloseHandler] => WatchFilesDialog
}

class WatchFilesDialog(extraOnCloseHandler: Option[ExtraOnCloseHandler])(val myIde: MyIde, myClient: MyClient, val pairEventListeners: PairEventListeners, isSubPath: IsSubPath, getSelectedFromFileTree: GetSelectedFromFileTree, getListItems: GetListItems, removeSelectedItemsFromList: RemoveSelectedItemsFromList, initListItems: InitListItems, initFileTree: InitFileTree, showErrorDialog: ShowErrorDialog, val currentProject: IdeaProjectImpl) extends _WatchFilesDialog with JDialogSupport {

  setTitle("Choose the files you want to pair with others")
  setSize(Size(600, 400))

  onWindowOpened(init(myClient.serverWatchingFiles))
  onClick(okButton)(publishWatchFilesRequestToServer())
  onClick(okButton)(extraOnCloseHandler.foreach(_()))
  onClick(closeButton)(closeDialog())
  onClick(closeButton)(extraOnCloseHandler.foreach(_()))
  onClick(watchButton)(watchSelectedFiles())
  onClick(deWatchButton)(deWatchSelectedFiles())

  private def publishWatchFilesRequestToServer() = {
    val future = myClient.publishEvent(WatchFilesRequest(getListItems(watchingList)))
    future.onSuccess { case _ => closeDialog() }
    future.onFailure { case e: Throwable => showErrorDialog(message = e.toString) }
  }

  private def closeDialog(): Unit = dispose()

  private def deWatchSelectedFiles() = {
    removeSelectedItemsFromList(watchingList)
    init(getListItems(watchingList))
  }

  private def watchSelectedFiles() = {
    init(getSelectedFromFileTree(workingTree) ++: getListItems(watchingList))
  }

  def init(watchingFiles: Seq[String]): Unit = {
    val simplified = removeDuplicatePaths(watchingFiles)
    initFileTree(workingTree, !isInPathList(_, simplified))
    initListItems(watchingList, simplified.sorted)
  }

  def removeDuplicatePaths(paths: Seq[String]): Seq[String] = {
    paths.foldLeft(List.empty[String]) {
      case (result, item) => result.headOption match {
        case Some(prev) => if (isSubPath(item, prev)) result else item :: result
        case _ => item :: result
      }
    }.reverse
  }

  def isInPathList(file: MyFile, paths: Seq[String]): Boolean = {
    val relativePath = currentProject.getRelativePath(file.path)
    paths.exists(p => relativePath == Some(p) || relativePath.exists(_.startsWith(p + "/")))
  }

}
