package com.thoughtworks.pli.remotepair.idea.core

import javax.swing.tree.DefaultMutableTreeNode

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.utils.PathUtils

trait ProjectFilesSupport {
  this: ProjectPathSupport =>

  case class FileTree(root: FileTreeNode)

  case class FileTreeNode(data: MyTreeNodeData) extends DefaultMutableTreeNode(data) {
    override def hashCode(): Int = {
      data.file.hashCode()
    }

    override def equals(o: scala.Any): Boolean = o match {
      case d: DefaultMutableTreeNode => d.getUserObject match {
        case dd: MyTreeNodeData => dd.file == data.file
        case _ => false
      }
      case _ => false
    }
  }

  case class MyTreeNodeData(file: VirtualFile) {
    override def toString: String = file.getName
  }

  def getAllPairableFiles(ignoredFiles: Seq[String]): Seq[VirtualFile] = {
    val tree = buildFileTree(getBaseDir, ignoredFiles)
    toList(tree).filterNot(_.isDirectory).filterNot(_.getFileType.isBinary)
  }

  private def buildFileTree(rootDir: VirtualFile, ignoredFiles: Seq[String]): FileTree = {
    def fetchChildFiles(node: DefaultMutableTreeNode): Unit = {
      val data = node.getUserObject.asInstanceOf[MyTreeNodeData]
      if (data.file.isDirectory) {
        data.file.getChildren.foreach { c =>
          if (!isIgnored(c, ignoredFiles)) {
            val child = new FileTreeNode(MyTreeNodeData(c))
            node.add(child)
            fetchChildFiles(child)
          }
        }
      }
    }
    val rootNode = new FileTreeNode(MyTreeNodeData(rootDir))
    fetchChildFiles(rootNode)
    FileTree(rootNode)
  }

  private def isIgnored(file: VirtualFile, ignoredFiles: Seq[String]): Boolean = {
    val relativePath = getRelativePath(file)
    ignoredFiles.exists(base => PathUtils.isSubPathOf(relativePath, base))
  }

  private def toList(tree: FileTree): List[VirtualFile] = {
    def fetchChildren(node: FileTreeNode, result: List[VirtualFile]): List[VirtualFile] = {
      if (node.getChildCount == 0) result
      else {
        val nodes = (0 until node.getChildCount).map(node.getChildAt).map(_.asInstanceOf[FileTreeNode]).toList
        nodes.foldLeft(nodes.map(_.data.file) ::: result) {
          case (all, child) => fetchChildren(child, all)
        }
      }
    }
    fetchChildren(tree.root, Nil)
  }

}
