package com.thoughtworks.pli.remotepair.idea.dialogs.tree

import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel

import com.thoughtworks.pli.remotepair.idea.core.GetProjectBaseDir
import com.thoughtworks.pli.remotepair.idea.dialogs.ResetTreeWithExpandedPathKept

class InitFileTree(resetTreeWithExpandedPathKept: ResetTreeWithExpandedPathKept, createFileTreeNode: CreateFileTreeNode, getProjectBaseDir: GetProjectBaseDir) {
  def apply(tree: JTree, ignoredPaths: Seq[String]): Unit = {
    resetTreeWithExpandedPathKept(tree) {
      val root = createFileTreeNode(getProjectBaseDir(), ignoredPaths)
      tree.setModel(new DefaultTreeModel(root))
    }
  }
}
