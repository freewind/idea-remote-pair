package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.remotepair.idea.project.GetProjectBaseDir
import com.thoughtworks.pli.remotepair.core.tree.{CreateFileTree, FileTreeNode}
import com.thoughtworks.pli.remotepair.core.IsInPathList

class GetAllWatchingFiles(getProjectBaseDir: GetProjectBaseDir, getServerWatchingFiles: GetServerWatchingFiles, createFileTree: CreateFileTree, isInPathList: IsInPathList) {
  def apply(): Seq[MyFile] = {
    val tree = createFileTree(getProjectBaseDir(), isInPathList(_, getServerWatchingFiles()))
    toList(tree).filterNot(_.isDirectory).filterNot(_.getFileType.isBinary)
  }

  private def toList(tree: FileTreeNode): List[MyFile] = {
    def fetchChildren(node: FileTreeNode, result: List[MyFile]): List[MyFile] = {
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
