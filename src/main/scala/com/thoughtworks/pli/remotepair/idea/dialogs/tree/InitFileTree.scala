package com.thoughtworks.pli.remotepair.idea.dialogs.tree

import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel

import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.dialogs.ResetTreeWithExpandedPathKept

class InitFileTree(currentProject: RichProject, resetTreeWithExpandedPathKept: ResetTreeWithExpandedPathKept, createFileTreeNode: CreateFileTreeNode) {
  def apply(tree: JTree, ignoredPaths: Seq[String]): Unit = {
    resetTreeWithExpandedPathKept(tree) {
      val root = createFileTreeNode(currentProject.getBaseDir, ignoredPaths)
      tree.setModel(new DefaultTreeModel(root))
    }
  }
}
