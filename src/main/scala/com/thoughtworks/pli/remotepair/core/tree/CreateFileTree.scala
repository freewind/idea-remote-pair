package com.thoughtworks.pli.remotepair.core.tree

import javax.swing.tree.DefaultMutableTreeNode

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.file.{GetRelativePath, GetFileChildren, GetFileName, IsDirectory}

class CreateFileTree(getRelativePath: GetRelativePath, isDirectory: IsDirectory, getFileChildren: GetFileChildren, fileTreeNodeDataFactory: FileTreeNodeData.Factory) {

  def apply(dir: VirtualFile, filterFile: VirtualFile => Boolean): FileTreeNode = {
    val rootNode = new FileTreeNode(fileTreeNodeDataFactory(dir))
    fetchChildFiles(rootNode, filterFile)
    rootNode
  }

  private def fetchChildFiles(node: DefaultMutableTreeNode, filterFile: VirtualFile => Boolean): Unit = {
    val data = node.getUserObject.asInstanceOf[FileTreeNodeData]
    if (isDirectory(data.file)) {
      getFileChildren(data.file).foreach { childFile =>
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
  type Factory = (VirtualFile) => FileTreeNodeData
}

class FileTreeNodeData(val file: VirtualFile)(getFileName: GetFileName) {
  override def toString: String = getFileName(file)
}


