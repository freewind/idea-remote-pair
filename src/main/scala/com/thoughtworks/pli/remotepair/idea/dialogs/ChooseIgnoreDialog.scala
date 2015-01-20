package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.DefaultListModel
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel, TreePath}

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.IgnoreFilesRequest
import com.thoughtworks.pli.remotepair.idea.core._

import scala.collection.mutable.ListBuffer
import scala.io.Source

class ChooseIgnoreDialog(override val currentProject: RichProject)
  extends _ChooseIgnoreDialog with InvokeLater with PublishEvents with CurrentProjectHolder with ChooseIgnoreDialogSupport with JDialogSupport {
  dialog =>

  init()

  clickOn(okButton) {
    currentProject.connection.foreach { conn =>
      try {
        conn.publish(IgnoreFilesRequest(ignoredFiles))
      } catch {
        case e: Throwable => currentProject.showErrorDialog(message = e.toString)
      }
    }
  }

}

case class MyTreeNodeData(file: VirtualFile) {
  override def toString: String = file.getName
}

trait ChooseIgnoreDialogSupport {
  this: _ChooseIgnoreDialog with CurrentProjectHolder with JDialogSupport =>

  val myFileSummaries = currentProject.getAllPairableFiles(currentProject.ignoredFiles).map(currentProject.getFileSummary)

  private val workingDir = currentProject.getBaseDir

  init()

  clickOn(btnMoveToIgnored) {
    val newIgnored = getSelectedFromWorkingTree.map(d => currentProject.getRelativePath(d.file))
    addIgnoreFiles(newIgnored)
    init()
  }

  private def addIgnoreFiles(newIgnored: Seq[String]): Unit = {
    val files = newIgnored.toList ::: ignoredFiles.toList
    ignoredFiles = files
  }

  clickOn(btnRestoreFromIgnored) {
    ignoredList.getSelectedValues.foreach(getListModel.removeElement)
    init()
  }

  def findGitIgnoreFile: Option[VirtualFile] = currentProject.getFileByRelative("/.gitignore")

  clickOn(guessFromGitignoreButton) {
    def getIdeaDotFiles: Seq[String] = {
      val files = currentProject.getBaseDir.getChildren.filter(file => file.getName.startsWith(".") || file.getName.endsWith(".iml")).map(currentProject.getRelativePath)
      files.toSeq filter (_ != "/.gitignore")
    }
    def readLines(f: VirtualFile) = {
      val content = currentProject.getFileContent(f)
      Source.fromString(content.text).getLines().toList.map(_.trim).filterNot(_.isEmpty).filterNot(_.startsWith("#"))
    }
    val filesFromGitIgnore = findGitIgnoreFile.map(readLines).getOrElse(Nil).flatMap(toRealPath)
    addIgnoreFiles(filesFromGitIgnore)
    addIgnoreFiles(getIdeaDotFiles)
  }

  private def toRealPath(patten: String): Option[String] = {
    def fixCurrentPrefix(p: String) = p.replaceAll("^[.]/", "/")
    def fixGlobalPrefix(p: String) = if (p.startsWith("/")) p else "/" + p
    def removeEndingStar(p: String) = p.replaceAll("[*]+$", "")
    def removeEndingSlash(p: String) = p.replaceAll("[/]$", "")
    def isNotValid(path: String) = path.contains("*") || path.endsWith("\\") || currentProject.getFileByRelative(path).isEmpty

    val fixedPath = Option(patten).map(fixCurrentPrefix).map(fixGlobalPrefix).map(removeEndingStar).map(removeEndingSlash)
    fixedPath.filterNot(isNotValid)
  }

  private def getListModel: DefaultListModel = {
    ignoredList.getModel.asInstanceOf[DefaultListModel]
  }

  private def getSelectedFromWorkingTree = {
    workingTree.getSelectionModel.getSelectionPaths
      .map(_.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode])
      .map(_.getUserObject.asInstanceOf[MyTreeNodeData])
  }

  private def getExpandedPaths = {
    val expanded = workingTree.getExpandedDescendants(workingTree.getPathForRow(0))
    val result = new ListBuffer[TreePath]
    while (expanded != null && expanded.hasMoreElements) {
      result += expanded.nextElement()
    }
    result.toSeq
  }

  private def withExpandedPathKept(f: => Any) = {
    val expandedPaths = getExpandedPaths
    f
    expandedPaths.foreach(workingTree.expandPath)
  }

  def ignoredFiles: Seq[String] = {
    val model = ignoredList.getModel
    (0 until model.getSize).map(model.getElementAt).map(_.asInstanceOf[String]).toList
  }

  val igggg = currentProject.projectInfo.toList.flatMap(_.ignoredFiles)

  def ignoredFiles_=(files: Seq[String]): Unit = {
    val listModel = new DefaultListModel()
    simplifyIgnored(files.sorted).foreach(listModel.addElement)
    ignoredList.setModel(listModel)

    init()
  }

  private def simplifyIgnored(files: Seq[String]) = {
    files.foldLeft(List.empty[String]) {
      case (result, item) => result.headOption match {
        case Some(prev) => if (isSubpath(item, prev)) result else item :: result
        case _ => item :: result
      }
    }.reverse
  }

  private def isSubpath(sub: String, parent: String) = sub == parent || sub.startsWith(parent + "/")

  def init(): Unit = withExpandedPathKept {
    val rootNode = createNodes(workingDir, ignoredFiles)
    workingTree.setModel(new DefaultTreeModel(rootNode))
  }

  class FileTreeNode(data: MyTreeNodeData) extends DefaultMutableTreeNode(data) {
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

  private def createNodes(file: VirtualFile, ignoredFiles: Seq[String]) = {
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
    val rootNode = new FileTreeNode(MyTreeNodeData(file))
    fetchChildFiles(rootNode)
    rootNode
  }

  private def isIgnored(file: VirtualFile, ignoredFiles: Seq[String]): Boolean = {
    val relativePath = currentProject.getRelativePath(file)
    ignoredFiles.exists(p => relativePath == p || relativePath.startsWith(p + "/"))
  }
}
