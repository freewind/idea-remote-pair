package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.JTree
import javax.swing.tree.TreePath

import scala.collection.mutable.ListBuffer

class ResetTreeWithExpandedPathKept {
  def apply(tree: JTree)(f: => Any): Unit = withExpandedPathKept(tree)(f)

  private def withExpandedPathKept(tree: JTree)(f: => Any) = {
    val expandedPaths = getExpandedPaths(tree)
    f
    expandedPaths.foreach(tree.expandPath)
  }

  private def getExpandedPaths(tree: JTree) = {
    val expanded = tree.getExpandedDescendants(tree.getPathForRow(0))
    val result = new ListBuffer[TreePath]
    while (expanded != null && expanded.hasMoreElements) {
      result += expanded.nextElement()
    }
    result.toSeq
  }

}
