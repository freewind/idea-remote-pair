package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.remotepair.core.IsInPathList
import com.thoughtworks.pli.remotepair.core.models.{MyProject, MyFile}
import com.thoughtworks.pli.remotepair.core.tree.{CreateFileTree, FileTreeNode}

class GetAllWatchingFiles(currentProject: MyProject, getServerWatchingFiles: GetServerWatchingFiles, createFileTree: CreateFileTree, isInPathList: IsInPathList) {
  def apply(): Seq[MyFile] = {
    val tree = createFileTree(currentProject.getBaseDir, isInPathList(_, getServerWatchingFiles()))
    toList(tree).filterNot(_.isDirectory).filterNot(_.isBinary)
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
