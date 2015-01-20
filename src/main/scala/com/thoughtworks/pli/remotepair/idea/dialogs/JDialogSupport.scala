package com.thoughtworks.pli.remotepair.idea.dialogs

import java.awt.event._
import javax.swing._

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent
import com.thoughtworks.pli.remotepair.idea.core.{CurrentProjectHolder, InvokeLater}

trait JDialogSupport extends InvokeLater {
  this: JDialog with CurrentProjectHolder =>

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
    onWindowOpened(currentProject.eventHandler.addReadMonitor(monitor))
    onWindowClosed(currentProject.eventHandler.removeReadMonitor(monitor))
  }

  def monitorWrittenEvent(monitor: PairEvent => Any) = {
    onWindowOpened(currentProject.eventHandler.addWrittenMonitor(monitor))
    onWindowClosed(currentProject.eventHandler.removeWrittenMonitor(monitor))
  }

  def clickOn(button: JButton)(f: => Any) = {
    button.addActionListener(new ActionListener {
      override def actionPerformed(actionEvent: ActionEvent): Unit = invokeLater(f)
    })
  }

  def showOnCenter(): Unit = {
    this.pack()
    this.setLocationRelativeTo(currentProject.getWindow())
    this.setVisible(true)
  }

}
