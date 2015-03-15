package com.thoughtworks.pli.remotepair.idea.dialogs.tree

import javax.swing.tree.DefaultMutableTreeNode

import com.thoughtworks.pli.remotepair.idea.core.FileTreeNodeData

class FileTreeNode(data: FileTreeNodeData) extends DefaultMutableTreeNode(data) {
  override def hashCode(): Int = {
    data.file.hashCode()
  }

  override def equals(o: scala.Any): Boolean = o match {
    case d: DefaultMutableTreeNode => d.getUserObject match {
      case dd: FileTreeNodeData => dd.file == data.file
      case _ => false
    }
    case _ => false
  }
}
