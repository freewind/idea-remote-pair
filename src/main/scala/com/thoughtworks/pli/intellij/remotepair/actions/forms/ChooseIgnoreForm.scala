package com.thoughtworks.pli.intellij.remotepair.actions.forms

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.DefaultListModel
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel, TreePath}

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.RichProject

import scala.collection.mutable.ListBuffer

case class MyTreeNodeData(file: VirtualFile) {
  override def toString: String = file.getName
}

class ChooseIgnoreForm(currentProject: RichProject) extends _ChooseIgnoreForm {

  private var _workingDir: VirtualFile = _

  def setWorkingDir(file: VirtualFile): Unit = {
    _workingDir = file
    init()
  }

  getBtnMoveToIgnored.addActionListener(new ActionListener {
    override def actionPerformed(actionEvent: ActionEvent): Unit = {
      val files = getSelectedFromWorkingTree.map(d => currentProject.getRelativePath(d.file)).toList ::: ignoredFiles.toList
      ignoredFiles = files
      init()
    }
  })

  getBtnRestoreFromIgnored.addActionListener(new ActionListener {
    override def actionPerformed(actionEvent: ActionEvent): Unit = {
      getIgnoredList.getSelectedValues.foreach(getListModel.removeElement)
      init()
    }
  })

  private def getListModel: DefaultListModel = {
    getIgnoredList.getModel.asInstanceOf[DefaultListModel]
  }

  private def getSelectedFromWorkingTree = {
    getWorkingTree.getSelectionModel.getSelectionPaths
      .map(_.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode])
      .map(_.getUserObject.asInstanceOf[MyTreeNodeData])
  }

  private def getExpandedPaths = {
    val expanded = getWorkingTree.getExpandedDescendants(getWorkingTree.getPathForRow(0))
    val result = new ListBuffer[TreePath]
    while (expanded != null && expanded.hasMoreElements) {
      result += expanded.nextElement()
    }
    result.toSeq
  }

  private def withExpandedPathKept(f: => Any) = {
    val expandedPaths = getExpandedPaths
    f
    expandedPaths.foreach(getWorkingTree.expandPath)
  }

  def ignoredFiles: Seq[String] = {
    val model = getIgnoredList.getModel
    (0 until model.getSize).map(model.getElementAt).map(_.asInstanceOf[String]).toList
  }

  def ignoredFiles_=(files: Seq[String]): Unit = {
    val listModel = new DefaultListModel()
    files.sorted.foreach(listModel.addElement)
    getIgnoredList.setModel(listModel)

    init()
  }

  private def init(): Unit = withExpandedPathKept {
    val rootNode = createNodes(_workingDir, ignoredFiles)
    getWorkingTree.setModel(new DefaultTreeModel(rootNode))
  }

  class FileTreeNode(data: MyTreeNodeData) extends DefaultMutableTreeNode(data) {
    override def hashCode(): Int = {
      data.file.hashCode()
    }

    override def equals(o: scala.Any): Boolean = o match {
      case d: DefaultMutableTreeNode => d.getUserObject match {
        case dd: MyTreeNodeData => dd.file == data.file
        case _ => false
      }
      case _ => false
    }
  }

  private def createNodes(file: VirtualFile, ignoreFiles: Seq[String]) = {
    def fetchChildFiles(node: DefaultMutableTreeNode): Unit = {
      val data = node.getUserObject.asInstanceOf[MyTreeNodeData]
      if (data.file.isDirectory) {
        data.file.getChildren.foreach { c =>
          if (!isIgnored(c, ignoredFiles)) {
            val child = new FileTreeNode(MyTreeNodeData(c))
            node.add(child)
            fetchChildFiles(child)
          }
        }
      }
    }
    val rootNode = new FileTreeNode(MyTreeNodeData(file))
    fetchChildFiles(rootNode)
    rootNode
  }

  private def isIgnored(file: VirtualFile, ignoreFiles: Seq[String]): Boolean = {
    val relativePath = currentProject.getRelativePath(file)
    ignoredFiles.exists(p => relativePath == p || relativePath.startsWith(p + "/"))
  }
}
