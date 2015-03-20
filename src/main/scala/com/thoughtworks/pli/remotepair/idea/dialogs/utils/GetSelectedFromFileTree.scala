package com.thoughtworks.pli.remotepair.idea.dialogs.utils

import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

import com.thoughtworks.pli.remotepair.idea.core.GetRelativePath
import com.thoughtworks.pli.remotepair.idea.core.tree.FileTreeNodeDataFactory.FileTreeNodeData

class GetSelectedFromFileTree(getRelativePath: GetRelativePath) {
  def apply(tree: JTree): Seq[String] = {
    tree.getSelectionModel.getSelectionPaths
      .map(_.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode])
      .map(_.getUserObject.asInstanceOf[FileTreeNodeData])
      .flatMap(d => getRelativePath(d.file))
  }
}
