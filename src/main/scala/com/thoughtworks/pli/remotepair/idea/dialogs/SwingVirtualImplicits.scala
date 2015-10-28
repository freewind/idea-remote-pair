package com.thoughtworks.pli.remotepair.idea.dialogs

import java.awt.event.{ActionEvent, ActionListener, WindowAdapter, WindowEvent}
import javax.swing._
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel, TreePath}

import com.thoughtworks.pli.remotepair.core.models.MyFile

import scala.collection.mutable.ListBuffer

object SwingVirtualImplicits {

  implicit class RichButton(button: JButton) {
    def text_=(value: String): Unit = button.setText(value)
    def onClick(f: => Unit): Unit = button.addActionListener(new ActionListener {
      def actionPerformed(actionEvent: ActionEvent): Unit = f
    })
    def enabled_=(value: Boolean): Unit = button.setEnabled(value)
    def enabled: Boolean = button.isEnabled
    def requestFocus(): Unit = button.requestFocus()
  }

  implicit class RichInputField(input: JTextField) {
    def text: String = input.getText
    def text_=(value: String): Unit = input.setText(value)
    def requestFocus(): Unit = input.requestFocus()
  }

  implicit class RichCheckBox(checkbox: JCheckBox) {
    def isSelected: Boolean = checkbox.isSelected
    def requestFocus(): Unit = checkbox.requestFocus()
  }

  implicit class RichDialog(dialog: JDialog) {
    def dispose(): Unit = dialog.dispose()
    def onClose(f: => Unit): Unit = dialog.addWindowListener(new WindowAdapter {
      override def windowClosed(windowEvent: WindowEvent): Unit = f
    })
    def onOpen(f: => Unit): Unit = dialog.addWindowListener(new WindowAdapter {
      override def windowOpened(windowEvent: WindowEvent): Unit = f
    })
    def title: String = dialog.getTitle
    def title_=(title: String): Unit = dialog.setTitle(title)
    def requestFocus(): Unit = dialog.requestFocus()
  }

  implicit class RichLabel(label: JLabel) {
    def text: String = label.getText
    def text_=(value: String): Unit = {
      label.setText(value)
    }
    def visible_=(value: Boolean): Unit = label.setVisible(value)
    def visible: Boolean = label.isVisible
    def requestFocus(): Unit = label.requestFocus()
  }

  implicit class RichProgressBar(progressBar: JProgressBar) {
    def max: Int = progressBar.getMaximum
    def max_=(value: Int): Unit = progressBar.setMaximum(value)
    def value_=(value: Int): Unit = progressBar.setValue(value)
    def value: Int = progressBar.getValue
    def requestFocus(): Unit = progressBar.requestFocus()
  }

  implicit class RichList(list: JList) {
    def items: Seq[String] = {
      val model = list.getModel
      (0 until model.getSize).map(model.getElementAt).toSeq.asInstanceOf[Seq[String]]
    }
    def items_=(values: Seq[String]): Unit = {
      val listModel = new DefaultListModel()
      values.foreach(listModel.addElement)
      list.setModel(listModel)
    }
    def selectedItems: Seq[String] = list.getSelectedValues.map(_.toString)
    def removeItems(values: Seq[String]): Unit = {
      val listModel = list.getModel.asInstanceOf[DefaultListModel]
      values.foreach(listModel.removeElement)
    }
    def requestFocus(): Unit = list.requestFocus()
  }

  implicit class RichFileTree(tree: JTree) {
    def init(baseDir: MyFile, filter: (MyFile) => Boolean): Unit = {
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
    def selectedFiles: Seq[String] = {
      tree.getSelectionModel.getSelectionPaths
        .map(_.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode])
        .map(_.getUserObject.asInstanceOf[FileTreeNodeData])
        .flatMap(d => d.file.relativePath)
    }
    def requestFocus(): Unit = tree.requestFocus()
  }

}
