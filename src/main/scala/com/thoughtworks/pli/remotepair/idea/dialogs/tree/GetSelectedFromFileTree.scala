package com.thoughtworks.pli.remotepair.idea.dialogs.tree

import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

import com.thoughtworks.pli.remotepair.idea.core.FileTreeNodeData
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

class GetSelectedFromFileTree(currentProject: RichProject) {
  def apply(tree: JTree): Seq[String] = {
    tree.getSelectionModel.getSelectionPaths
      .map(_.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode])
      .map(_.getUserObject.asInstanceOf[FileTreeNodeData])
      .flatMap(d => currentProject.getRelativePath(d.file))
  }
}
