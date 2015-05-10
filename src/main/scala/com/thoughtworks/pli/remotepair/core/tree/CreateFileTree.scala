package com.thoughtworks.pli.remotepair.core.tree

import javax.swing.tree.DefaultMutableTreeNode

import com.thoughtworks.pli.remotepair.core.models.MyFile

class CreateFileTree(fileTreeNodeDataFactory: FileTreeNodeData.Factory) {

  def apply(dir: MyFile, filterFile: MyFile => Boolean): FileTreeNode = {
    val rootNode = new FileTreeNode(fileTreeNodeDataFactory(dir))
    fetchChildFiles(rootNode, filterFile)
    rootNode
  }

  private def fetchChildFiles(node: DefaultMutableTreeNode, filterFile: MyFile => Boolean): Unit = {
    val data = node.getUserObject.asInstanceOf[FileTreeNodeData]
    if (data.file.isDirectory) {
      data.file.children.foreach { childFile =>
        if (filterFile(childFile)) {
          val child = new FileTreeNode(fileTreeNodeDataFactory(childFile))
          node.add(child)
          fetchChildFiles(child, filterFile)
        }
      }
    }
  }

}

case class FileTreeNode(data: FileTreeNodeData) extends DefaultMutableTreeNode(data) {
  override def hashCode(): Int = {
    data.file.hashCode()
  }

  override def equals(o: scala.Any): Boolean = o match {
    case d: DefaultMutableTreeNode => d.getUserObject match {
      case dd: FileTreeNodeData => dd.file == data.file
      case _ => false
    }
    case _ => false
  }
}

object FileTreeNodeData {
  type Factory = (MyFile) => FileTreeNodeData
}

class FileTreeNodeData(val file: MyFile) {
  override def toString: String = file.name
}


