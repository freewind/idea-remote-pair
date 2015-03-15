package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.WatchFilesRequest
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.dialogs.list.{GetListItems, InitListItems}
import com.thoughtworks.pli.remotepair.idea.dialogs.tree.{GetSelectedFromFileTree, InitFileTree}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater
import scala.concurrent.ExecutionContext.Implicits.global

object WatchFilesDialogFactory {
  type WatchFilesDialog = WatchFilesDialogFactory#create
}

case class WatchFilesDialogFactory(currentProject: RichProject, invokeLater: InvokeLater, publishEvent: PublishEvent, pairEventListeners: PairEventListeners, isSubPath: IsSubPath, getServerWatchingFiles: GetServerWatchingFiles, getSelectedPathFromFileTree: GetSelectedFromFileTree, getListItems: GetListItems, removeSelectedItems: RemoveSelectedItemsFromList, removeDuplicatePaths: RemoveDuplicatePaths, initListItems: InitListItems, initFileTree: InitFileTree) {
  factory =>

  case class create() extends _WatchFilesDialog with JDialogSupport {
    def invokeLater = factory.invokeLater
    def currentProject = factory.currentProject
    def pairEventListeners = factory.pairEventListeners

    setTitle("Choose the files you want to pair with others")
    setSize(Size(600, 400))

    onWindowOpened(init(getServerWatchingFiles()))
    onClick(okButton)(publishWatchFilesRequestToServer())
    onClick(closeButton)(closeDialog())
    onClick(watchButton)(watchSelectedFiles())
    onClick(deWatchButton)(deWatchSelectedFiles())

    private def publishWatchFilesRequestToServer() = {
      val future = publishEvent(WatchFilesRequest(getListItems(watchingList)))
      future.onSuccess { case _ => closeDialog()}
      future.onFailure { case e: Throwable => currentProject.showErrorDialog(message = e.toString)}
    }

    private def closeDialog(): Unit = dispose()

    private def deWatchSelectedFiles() = {
      removeSelectedItems(watchingList)
      init(getListItems(watchingList))
    }

    private def watchSelectedFiles() = {
      init(getSelectedPathFromFileTree(workingTree) ++: getListItems(watchingList))
    }

    def init(watchingFiles: Seq[String]): Unit = {
      val simplified = removeDuplicatePaths(watchingFiles)
      initFileTree(workingTree, simplified)
      initListItems(watchingList, simplified.sorted)
    }
  }

}
