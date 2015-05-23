package com.thoughtworks.pli.remotepair.idea.dialogs.utils

import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

import com.thoughtworks.pli.remotepair.core.utils.FileTreeNodeData

class GetSelectedFromFileTree() {
  def apply(tree: JTree): Seq[String] = {
    tree.getSelectionModel.getSelectionPaths
      .map(_.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode])
      .map(_.getUserObject.asInstanceOf[FileTreeNodeData])
      .flatMap(d => d.file.relativePath)
  }
}
