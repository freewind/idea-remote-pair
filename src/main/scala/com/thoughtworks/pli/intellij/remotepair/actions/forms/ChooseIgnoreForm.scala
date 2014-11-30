package com.thoughtworks.pli.intellij.remotepair.actions.forms

import java.awt.event.{ActionEvent, ActionListener}
import java.io.File
import javax.swing.DefaultListModel
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel, TreePath}

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.RichProject
import org.apache.commons.io.FileUtils

import scala.collection.mutable.ListBuffer
import scala.io.Source

case class MyTreeNodeData(file: VirtualFile) {
  override def toString: String = file.getName
}

class ChooseIgnoreForm(currentProject: RichProject) extends _ChooseIgnoreForm {

  private var _workingDir: VirtualFile = _

  def setWorkingDir(file: VirtualFile): Unit = {
    _workingDir = file
    init()
  }

  getBtnMoveToIgnored.addActionListener(new ActionListener {
    override def actionPerformed(actionEvent: ActionEvent): Unit = {
      val newIgnored = getSelectedFromWorkingTree.map(d => currentProject.getRelativePath(d.file))
      addIgnoreFiles(newIgnored)
      init()
    }
  })

  getBtnIgnoreDotFiles.addActionListener(new ActionListener {
    override def actionPerformed(actionEvent: ActionEvent): Unit = {
      addIgnoreFiles(getDotFiles)
    }
    private def getDotFiles: Seq[String] = {
      currentProject.getBaseDir.getChildren.filter(_.getName.startsWith(".")).map(currentProject.getRelativePath)
    }
  })

  private def addIgnoreFiles(newIgnored: Seq[String]): Unit = {
    val files = newIgnored.toList ::: ignoredFiles.toList
    ignoredFiles = files
  }

  getBtnRestoreFromIgnored.addActionListener(new ActionListener {
    override def actionPerformed(actionEvent: ActionEvent): Unit = {
      getIgnoredList.getSelectedValues.foreach(getListModel.removeElement)
      init()
    }
  })

  def findGitIgnoreFile: Option[File] = currentProject.getFileByRelative(".gitignore").map(_.getPath).map(new File(_))

  getGuessFromGitignoreButton.addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent) = {
      def readLines(f: File) = {
        val content = FileUtils.readFileToString(f, "UTF-8")
        Source.fromString(content).getLines().toList.map(_.trim).filterNot(_.isEmpty).filterNot(_.startsWith("#"))
      }
      val newIgnored = findGitIgnoreFile.toList.flatMap(readLines).flatMap(toRealPath)
      addIgnoreFiles(newIgnored)
    }
  })

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
    getIgnoredList.getModel.asInstanceOf[DefaultListModel]
  }

  private def getSelectedFromWorkingTree = {
    getWorkingTree.getSelectionModel.getSelectionPaths
      .map(_.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode])
      .map(_.getUserObject.asInstanceOf[MyTreeNodeData])
  }

  private def getExpandedPaths = {
    val expanded = getWorkingTree.getExpandedDescendants(getWorkingTree.getPathForRow(0))
    val result = new ListBuffer[TreePath]
    while (expanded != null && expanded.hasMoreElements) {
      result += expanded.nextElement()
    }
    result.toSeq
  }

  private def withExpandedPathKept(f: => Any) = {
    val expandedPaths = getExpandedPaths
    f
    expandedPaths.foreach(getWorkingTree.expandPath)
  }

  def ignoredFiles: Seq[String] = {
    val model = getIgnoredList.getModel
    (0 until model.getSize).map(model.getElementAt).map(_.asInstanceOf[String]).toList
  }

  def ignoredFiles_=(files: Seq[String]): Unit = {
    val listModel = new DefaultListModel()
    simplifyIgnored(files.sorted).foreach(listModel.addElement)
    getIgnoredList.setModel(listModel)

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

  private def init(): Unit = withExpandedPathKept {
    val rootNode = createNodes(_workingDir, ignoredFiles)
    getWorkingTree.setModel(new DefaultTreeModel(rootNode))
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
