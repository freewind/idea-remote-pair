package com.thoughtworks.pli.remotepair.core.ui.dialogs

import javax.swing.JDialog

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent
import com.thoughtworks.pli.remotepair.idea.dialogs.SwingVirtualImplicits._
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

trait BaseVirtualDialog {
  this: JDialog =>

  def pairEventListeners: PairEventListeners

  def monitorReadEvent(monitor: PartialFunction[PairEvent, Any]) = {
    this.onOpen(pairEventListeners.addReadMonitor(monitor))
    this.onClose(pairEventListeners.removeReadMonitor(monitor))
  }

  def monitorWrittenEvent(monitor: PartialFunction[PairEvent, Any]) = {
    this.onOpen(pairEventListeners.addWrittenMonitor(monitor))
    this.onClose(pairEventListeners.removeWrittenMonitor(monitor))
  }

  def init(): Unit
  def showOnCenter(): Unit

}
