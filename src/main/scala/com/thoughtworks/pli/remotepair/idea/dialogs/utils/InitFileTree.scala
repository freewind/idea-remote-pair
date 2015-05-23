package com.thoughtworks.pli.remotepair.idea.dialogs.utils

import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel

import com.thoughtworks.pli.remotepair.core.models.{MyFile, MyProject}
import com.thoughtworks.pli.remotepair.core.utils.CreateFileTree
import com.thoughtworks.pli.remotepair.idea.dialogs.ResetTreeWithExpandedPathKept

class InitFileTree(currentProject: MyProject, resetTreeWithExpandedPathKept: ResetTreeWithExpandedPathKept, createFileTree: CreateFileTree) {
  def apply(tree: JTree, filterFile: MyFile => Boolean): Unit = {
    resetTreeWithExpandedPathKept(tree) {
      val root = createFileTree(currentProject.baseDir, filterFile)
      tree.setModel(new DefaultTreeModel(root))
    }
  }
}
