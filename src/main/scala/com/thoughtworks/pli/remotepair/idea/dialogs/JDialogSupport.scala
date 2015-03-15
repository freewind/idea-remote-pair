package com.thoughtworks.pli.remotepair.idea.dialogs

import java.awt.event._
import javax.swing._

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.PairEventListeners
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

trait JDialogSupport {
  this: JDialog =>

  def invokeLater: InvokeLater
  def currentProject: RichProject
  def pairEventListeners: PairEventListeners

  case class Size(width: Int, height: Int)

  private var size: Option[Size] = None

  setModal(true)
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  def setSize(size: Size) = this.size = Some(size)

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

  def monitorReadEvent(monitor: PartialFunction[PairEvent, Any]) = {
    onWindowOpened(pairEventListeners.addReadMonitor(monitor))
    onWindowClosed(pairEventListeners.removeReadMonitor(monitor))
  }

  def monitorWrittenEvent(monitor: PartialFunction[PairEvent, Any]) = {
    onWindowOpened(pairEventListeners.addWrittenMonitor(monitor))
    onWindowClosed(pairEventListeners.removeWrittenMonitor(monitor))
  }

  def onClick(button: JButton)(f: => Any) = {
    button.addActionListener(new ActionListener {
      override def actionPerformed(actionEvent: ActionEvent): Unit = invokeLater(f)
    })
  }

  def showOnCenter(): Unit = {
    this.pack()
    this.size.foreach(s => setSize(s.width, s.height))
    this.setLocationRelativeTo(currentProject.getWindow)
    this.setVisible(true)
  }

}
