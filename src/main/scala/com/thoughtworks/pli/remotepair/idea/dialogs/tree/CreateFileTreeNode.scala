package com.thoughtworks.pli.remotepair.idea.dialogs.tree

import javax.swing.tree.DefaultMutableTreeNode

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.core.{GetRelativePath, FileTreeNodeData}

class CreateFileTreeNode(getRelativePath: GetRelativePath) {
  def apply(dir: VirtualFile, ignoredPaths: Seq[String]): FileTreeNode = {
    createNodes(dir, ignoredPaths)
  }

  private def createNodes(file: VirtualFile, ignoredFiles: Seq[String]) = {
    def fetchChildFiles(node: DefaultMutableTreeNode): Unit = {
      val data = node.getUserObject.asInstanceOf[FileTreeNodeData]
      if (data.file.isDirectory) {
        data.file.getChildren.foreach { c =>
          if (!isIgnored(c, ignoredFiles)) {
            val child = new FileTreeNode(FileTreeNodeData(c))
            node.add(child)
            fetchChildFiles(child)
          }
        }
      }
    }
    val rootNode = new FileTreeNode(FileTreeNodeData(file))
    fetchChildFiles(rootNode)
    rootNode
  }

  private def isIgnored(file: VirtualFile, ignoredFiles: Seq[String]): Boolean = {
    val relativePath = getRelativePath(file)
    ignoredFiles.exists(p => relativePath == Some(p) || relativePath.exists(_.startsWith(p + "/")))
  }

}
