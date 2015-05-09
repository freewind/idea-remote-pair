package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.core.tree.{FileTreeNode, CreateFileTree}

class GetAllWatchingFiles(getProjectBaseDir: GetProjectBaseDir, getServerWatchingFiles: GetServerWatchingFiles, createFileTree: CreateFileTree, isInPathList: IsInPathList) {
  def apply(): Seq[VirtualFile] = {
    val tree = createFileTree(getProjectBaseDir(), isInPathList(_, getServerWatchingFiles()))
    toList(tree).filterNot(_.isDirectory).filterNot(_.getFileType.isBinary)
  }

  private def toList(tree: FileTreeNode): List[VirtualFile] = {
    def fetchChildren(node: FileTreeNode, result: List[VirtualFile]): List[VirtualFile] = {
      if (node.getChildCount == 0) result
      else {
        val nodes = (0 until node.getChildCount).map(node.getChildAt).map(_.asInstanceOf[FileTreeNode]).toList
        nodes.foldLeft(nodes.map(_.data.file) ::: result) {
          case (all, child) => fetchChildren(child, all)
        }
      }
    }
    fetchChildren(tree, Nil)
  }

}
