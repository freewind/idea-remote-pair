package com.thoughtworks.pli.remotepair.idea.dialogs.utils

import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.core.GetProjectBaseDir
import com.thoughtworks.pli.remotepair.idea.core.tree.CreateFileTree
import com.thoughtworks.pli.remotepair.idea.dialogs.ResetTreeWithExpandedPathKept

class InitFileTree(resetTreeWithExpandedPathKept: ResetTreeWithExpandedPathKept, createFileTree: CreateFileTree, getProjectBaseDir: GetProjectBaseDir) {
  def apply(tree: JTree, filterFile: VirtualFile => Boolean): Unit = {
    resetTreeWithExpandedPathKept(tree) {
      val root = createFileTree(getProjectBaseDir(), filterFile)
      tree.setModel(new DefaultTreeModel(root))
    }
  }
}
