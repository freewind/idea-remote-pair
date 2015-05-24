package com.thoughtworks.pli.remotepair.core.ui.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.PairEvent
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents.VirtualDialog
import com.thoughtworks.pli.remotepair.idea.listeners.PairEventListeners

trait BaseVirtualDialog {
  def dialog: VirtualDialog
  def pairEventListeners: PairEventListeners

  def monitorReadEvent(monitor: PartialFunction[PairEvent, Any]) = {
    dialog.onOpen(pairEventListeners.addReadMonitor(monitor))
    dialog.onClose(pairEventListeners.removeReadMonitor(monitor))
  }

  def monitorWrittenEvent(monitor: PartialFunction[PairEvent, Any]) = {
    dialog.onOpen(pairEventListeners.addWrittenMonitor(monitor))
    dialog.onClose(pairEventListeners.removeWrittenMonitor(monitor))
  }

  def showOnCenter(): Unit

}
