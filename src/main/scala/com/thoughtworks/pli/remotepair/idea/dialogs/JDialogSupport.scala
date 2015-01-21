package com.thoughtworks.pli.remotepair.idea.dialogs

import java.awt.event._
import javax.swing._

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent
import com.thoughtworks.pli.remotepair.idea.core.{CurrentProjectHolder, InvokeLater}

trait JDialogSupport extends InvokeLater {
  this: JDialog with CurrentProjectHolder =>

  def getContentPanel: JPanel

  setContentPane(getContentPane)
  setModal(true)
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  def onWindowOpened(action: => Any): Unit = {
    addWindowListener(new WindowAdapter {
      override def windowOpened(windowEvent: WindowEvent): Unit = {
        action
      }
    })
  }

  def onWindowClosed(action: => Any): Unit = {
    addWindowListener(new WindowAdapter {
      override def windowClosed(windowEvent: WindowEvent): Unit = {
        action
      }
    })
  }

  def monitorReadEvent(monitor: PairEvent => Any) = {
    onWindowOpened(currentProject.eventHandler.foreach(_.addReadMonitor(monitor)))
    onWindowClosed(currentProject.eventHandler.foreach(_.removeReadMonitor(monitor)))
  }

  def monitorWrittenEvent(monitor: PairEvent => Any) = {
    onWindowOpened(currentProject.eventHandler.foreach(_.addWrittenMonitor(monitor)))
    onWindowClosed(currentProject.eventHandler.foreach(_.removeWrittenMonitor(monitor)))
  }

  def clickOn(button: JButton)(f: => Any) = {
    button.addActionListener(new ActionListener {
      override def actionPerformed(actionEvent: ActionEvent): Unit = invokeLater(f)
    })
  }

  def showOnCenter(): Unit = {
    this.setSize(400, 500)
    this.pack()
    this.setLocationRelativeTo(currentProject.getWindow())
    this.setVisible(true)
  }

}
