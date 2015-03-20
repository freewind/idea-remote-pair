package com.thoughtworks.pli.remotepair.idea.core.tree

import javax.swing.tree.DefaultMutableTreeNode

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import com.thoughtworks.pli.remotepair.idea.core.tree.FileTreeNodeDataFactory.FileTreeNodeData
import com.thoughtworks.pli.remotepair.idea.core.{GetProjectBaseDir, GetRelativePath, GetServerWatchingFiles}

class BuildFileTree(isSubPath: IsSubPath, getProjectBaseDir: GetProjectBaseDir, getRelativePath: GetRelativePath, getServerWatchingFiles: GetServerWatchingFiles, fileTreeNodeDataFactory: FileTreeNodeDataFactory) {

  def apply(rootDir: VirtualFile, ignoredFiles: Seq[String]): FileTree = {
    def fetchChildFiles(node: DefaultMutableTreeNode): Unit = {
      val data = node.getUserObject.asInstanceOf[FileTreeNodeData]
      if (data.file.isDirectory) {
        data.file.getChildren.foreach { c =>
          if (!isIgnored(c, ignoredFiles)) {
            val child = new FileTreeNode(fileTreeNodeDataFactory.create(c))
            node.add(child)
            fetchChildFiles(child)
          }
        }
      }
    }
    val rootNode = new FileTreeNode(fileTreeNodeDataFactory.create(rootDir))
    fetchChildFiles(rootNode)
    FileTree(rootNode)
  }

  private def isIgnored(file: VirtualFile, ignoredFiles: Seq[String]): Boolean = {
    ignoredFiles.exists(base => getRelativePath(file).exists(p => isSubPath(p, base)))
  }

}

case class FileTree(root: FileTreeNode)
