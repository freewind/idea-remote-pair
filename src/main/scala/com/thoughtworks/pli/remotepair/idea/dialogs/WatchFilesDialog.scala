package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.{DefaultListModel, JList, JTree}
import javax.swing.tree.{TreePath, DefaultTreeModel, DefaultMutableTreeNode}

import com.thoughtworks.pli.intellij.remotepair.protocol.WatchFilesRequest
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyFile, MyIde}
import com.thoughtworks.pli.remotepair.core.utils.{CreateFileTree, FileTreeNodeData}
import com.thoughtworks.pli.remotepair.idea.dialogs.WatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global

object WatchFilesDialog {
  type ExtraOnCloseHandler = () => Unit
  type Factory = Option[ExtraOnCloseHandler] => WatchFilesDialog
}

class WatchFilesDialog(extraOnCloseHandler: Option[ExtraOnCloseHandler])(val myIde: MyIde, myClient: MyClient, val pairEventListeners: PairEventListeners, isSubPath: IsSubPath, val currentProject: IdeaProjectImpl, createFileTree: CreateFileTree) extends _WatchFilesDialog with JDialogSupport {

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
    future.onFailure { case e: Throwable => currentProject.showErrorDialog(message = e.toString) }
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

  private def getSelectedFromFileTree(tree: JTree): Seq[String] = {
    tree.getSelectionModel.getSelectionPaths
      .map(_.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode])
      .map(_.getUserObject.asInstanceOf[FileTreeNodeData])
      .flatMap(d => d.file.relativePath)
  }

  private def initFileTree(tree: JTree, filterFile: MyFile => Boolean): Unit = {
    resetTreeWithExpandedPathKept(tree) {
      val root = createFileTree(currentProject.baseDir, filterFile)
      tree.setModel(new DefaultTreeModel(root))
    }
  }

  def resetTreeWithExpandedPathKept(tree: JTree)(f: => Any): Unit = {
    def withExpandedPathKept(tree: JTree)(f: => Any) = {
      val expandedPaths = getExpandedPaths(tree)
      f
      expandedPaths.foreach(tree.expandPath)
    }

    def getExpandedPaths(tree: JTree) = {
      val expanded = tree.getExpandedDescendants(tree.getPathForRow(0))
      val result = new ListBuffer[TreePath]
      while (expanded != null && expanded.hasMoreElements) {
        result += expanded.nextElement()
      }
      result.toSeq
    }

    withExpandedPathKept(tree)(f)
  }

  private def removeSelectedItemsFromList(ignoredList: JList): Unit = {
    val listModel = ignoredList.getModel.asInstanceOf[DefaultListModel]
    ignoredList.getSelectedValues.foreach(listModel.removeElement)
  }

  private def getListItems(ignoredList: JList): Seq[String] = {
    val model = ignoredList.getModel
    (0 until model.getSize).map(model.getElementAt).map(_.asInstanceOf[String]).toList
  }

  private def initListItems(ignoredList: JList, items: Seq[String]): Unit = {
    val listModel = new DefaultListModel()
    items.foreach(listModel.addElement)
    ignoredList.setModel(listModel)
  }
}
