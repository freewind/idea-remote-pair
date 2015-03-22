package com.thoughtworks.pli.remotepair.idea.core.tree

import javax.swing.tree.DefaultMutableTreeNode

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.core.GetRelativePath
import com.thoughtworks.pli.remotepair.idea.core.files.{GetFileName, GetFileChildren, IsDirectory}
import com.thoughtworks.pli.remotepair.idea.core.tree.FileTreeNodeDataFactory.FileTreeNodeData

class CreateFileTree(getRelativePath: GetRelativePath, isDirectory: IsDirectory, getFileChildren: GetFileChildren, fileTreeNodeDataFactory: FileTreeNodeDataFactory) {

  def apply(dir: VirtualFile, filterFile: VirtualFile => Boolean): FileTreeNode = {
    val rootNode = new FileTreeNode(fileTreeNodeDataFactory.create(dir))
    fetchChildFiles(rootNode, filterFile)
    rootNode
  }

  private def fetchChildFiles(node: DefaultMutableTreeNode, filterFile: VirtualFile => Boolean): Unit = {
    val data = node.getUserObject.asInstanceOf[FileTreeNodeData]
    if (isDirectory(data.file)) {
      getFileChildren(data.file).foreach { childFile =>
        if (filterFile(childFile)) {
          val child = new FileTreeNode(fileTreeNodeDataFactory.create(childFile))
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

object FileTreeNodeDataFactory {
  type FileTreeNodeData = FileTreeNodeDataFactory#create
}

class FileTreeNodeDataFactory(getFileName: GetFileName) {
  case class create(file: VirtualFile) {
    override def toString: String = getFileName(file)
  }
}


