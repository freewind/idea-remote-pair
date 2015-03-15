package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.IgnoreFilesRequest
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.dialogs.list.{InitListItems, GetListItems}
import com.thoughtworks.pli.remotepair.idea.dialogs.tree.{GetSelectedFromFileTree, InitFileTree}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

object ChooseIgnoreDialogFactory {
  type ChooseIgnoreDialog = ChooseIgnoreDialogFactory#create
}

case class ChooseIgnoreDialogFactory(currentProject: RichProject, invokeLater: InvokeLater, publishEvent: PublishEvent, pairEventListeners: PairEventListeners, isSubPath: IsSubPath, getServerIgnoredFiles: GetServerIgnoredFiles, guessIgnoreFilesFrom: GuessIgnoreFilesFrom, getSelectedPathFromFileTree: GetSelectedFromFileTree, getListItems: GetListItems, removeSelectedItems: RemoveSelectedItems, removeDuplicatePaths: RemoveDuplicatePaths, initListItems: InitListItems, initFileTree: InitFileTree) {
  factory =>

  case class create() extends _ChooseIgnoreDialog with JDialogSupport {
    def invokeLater = factory.invokeLater
    def currentProject = factory.currentProject
    def pairEventListeners = factory.pairEventListeners

    setTitle("Choose the files ignored by the pair plugin")
    setSize(Size(600, 400))

    onWindowOpened(init(getServerIgnoredFiles()))
    onClick(okButton)(publishIgnoredFilesEventToServer())
    onClick(closeButton)(closeDialog())
    onClick(moveToIgnoredButton)(addSelectedFilesToIgnored())
    onClick(removeIgnoredButton)(removeIgnoredFiles())
    onClick(guessFromGitignoreButton)(addGuessedFilesToIgnored())

    private def publishIgnoredFilesEventToServer() = {
      val future = publishEvent(IgnoreFilesRequest(getListItems(ignoredList)))
      future.onSuccess { case _ => closeDialog()}
      future.onFailure { case e: Throwable => currentProject.showErrorDialog(message = e.toString)}
    }

    private def closeDialog(): Unit = dispose()

    private def addGuessedFilesToIgnored() = {
      init(guessIgnoreFilesFrom(currentProject.getBaseDir) ++: getListItems(ignoredList))
    }

    private def removeIgnoredFiles() = {
      removeSelectedItems(ignoredList)
      init(getListItems(ignoredList))
    }

    private def addSelectedFilesToIgnored() = {
      val newIgnored = getSelectedPathFromFileTree(workingTree)
      init(newIgnored ++: getListItems(ignoredList))
    }

    def init(ignoredFiles: Seq[String]): Unit = {
      val simplified = removeDuplicatePaths(ignoredFiles)
      initFileTree(workingTree, simplified)
      initListItems(ignoredList, simplified.sorted)
    }
  }

}
