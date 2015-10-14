package com.thoughtworks.pli.remotepair.idea.dialogs

import java.awt.event.{ActionEvent, ActionListener, WindowAdapter, WindowEvent}
import javax.swing._
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel, TreePath}

import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._

import scala.collection.mutable.ListBuffer

object SwingVirtualImplicits {

  implicit def button2virtual(button: JButton): VirtualButton = new VirtualButton {
    override def text_=(value: String): Unit = button.setText(value)
    override def onClick(f: => Unit): Unit = button.addActionListener(new ActionListener {
      override def actionPerformed(actionEvent: ActionEvent): Unit = f
    })
    override def enabled_=(value: Boolean): Unit = button.setEnabled(value)
    override def enabled: Boolean = button.isEnabled
    override def requestFocus(): Unit = button.requestFocus()
  }

  implicit def input2virtual(input: JTextField): VirtualInputField = new VirtualInputField {
    override def text: String = input.getText
    override def text_=(value: String): Unit = input.setText(value)
    override def requestFocus(): Unit = input.requestFocus()
  }

  implicit def checkbox2virtual(checkbox: JCheckBox): VirtualCheckBox = new VirtualCheckBox {
    override def isSelected: Boolean = checkbox.isSelected
    override def requestFocus(): Unit = checkbox.requestFocus()
  }

  implicit def dialog2virtual(dialog: JDialog): VirtualDialog = new VirtualDialog {
    override def dispose(): Unit = dialog.dispose()
    override def onClose(f: => Unit): Unit = dialog.addWindowListener(new WindowAdapter {
      override def windowClosed(windowEvent: WindowEvent): Unit = f
    })
    override def onOpen(f: => Unit): Unit = dialog.addWindowListener(new WindowAdapter {
      override def windowOpened(windowEvent: WindowEvent): Unit = f
    })
    override def title: String = dialog.getTitle
    override def title_=(title: String): Unit = dialog.setTitle(title)
    override def requestFocus(): Unit = dialog.requestFocus()
  }

  implicit def label2virtual(label: JLabel): VirtualLabel = new VirtualLabel {
    override def text: String = label.getText
    override def text_=(value: String): Unit = {
      label.setText(value)
    }
    override def visible_=(value: Boolean): Unit = label.setVisible(value)
    override def visible: Boolean = label.isVisible
    override def requestFocus(): Unit = label.requestFocus()
  }

  implicit def progressBar2virtual(progressBar: JProgressBar): VirtualProgressBar = new VirtualProgressBar {
    override def max: Int = progressBar.getMaximum
    override def max_=(value: Int): Unit = progressBar.setMaximum(value)
    override def value_=(value: Int): Unit = progressBar.setValue(value)
    override def value: Int = progressBar.getValue
    override def requestFocus(): Unit = progressBar.requestFocus()
  }

  implicit def list2virtual(list: JList[String]): VirtualList = new VirtualList {
    override def items: Seq[String] = {
      val model = list.getModel
      (0 until model.getSize).map(model.getElementAt).toList
    }
    override def items_=(values: Seq[String]): Unit = {
      val listModel = new DefaultListModel[String]()
      values.foreach(listModel.addElement)
      list.setModel(listModel)
    }
    override def selectedItems: Seq[String] = list.getSelectedValues.map(_.toString)
    override def removeItems(values: Seq[String]): Unit = {
      val listModel = list.getModel.asInstanceOf[DefaultListModel[String]]
      values.foreach(listModel.removeElement)
    }
    override def requestFocus(): Unit = list.requestFocus()
  }

  implicit def fileTree2virtual(tree: JTree): VirtualFileTree = new VirtualFileTree {
    override def init(baseDir: MyFile, filter: (MyFile) => Boolean): Unit = {
      def resetTreeWithExpandedPathKept(tree: JTree)(f: => Any): Unit = {
        def withExpandedPathKept(tree: JTree)(f: => Any) = {
          val expandedPaths = getExpandedPaths(tree)
          f
          expandedPaths.foreach(tree.expandPath)
        }

        def getExpandedPaths(tree: JTree) = {
          val expanded = tree.getExpandedDescendants(tree.getPathForRow(0))
          val result = new ListBuffer[TreePath]
          while (expanded != null && expanded.hasMoreElements) {
            result += expanded.nextElement()
          }
          result.toSeq
        }

        withExpandedPathKept(tree)(f)
      }

      def createFileTree(dir: MyFile, filter: MyFile => Boolean): FileTreeNode = {
        new CreateFileTree().apply(dir, filter)
      }
      resetTreeWithExpandedPathKept(tree) {
        val root = createFileTree(baseDir, filter)
        tree.setModel(new DefaultTreeModel(root))
      }
    }
    override def selectedFiles: Seq[String] = {
      tree.getSelectionModel.getSelectionPaths
        .map(_.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode])
        .map(_.getUserObject.asInstanceOf[FileTreeNodeData])
        .flatMap(d => d.file.relativePath)
    }
    override def requestFocus(): Unit = tree.requestFocus()
  }

}
