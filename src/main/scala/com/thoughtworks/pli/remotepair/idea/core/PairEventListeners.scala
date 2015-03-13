package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class PairEventListeners(currentProject: RichProject, invokeLater: InvokeLater) {

  type Monitor = PartialFunction[PairEvent, Any]
  @volatile private var readMonitors: Seq[Monitor] = Nil
  @volatile private var writtenMonitors: Seq[Monitor] = Nil

  def addReadMonitor(monitor: Monitor) = {
    readMonitors = readMonitors :+ monitor
  }
  def removeReadMonitor(monitor: Monitor) = {
    readMonitors = readMonitors.filterNot(_ == monitor)
  }
  def triggerReadMonitors(event: PairEvent): Unit = {
    readMonitors.filter(_.isDefinedAt(event)).foreach(monitor => invokeLater(monitor(event)))
  }

  def addWrittenMonitor(monitor: Monitor) = {
    writtenMonitors = writtenMonitors :+ monitor
  }
  def removeWrittenMonitor(monitor: Monitor) = {
    writtenMonitors = writtenMonitors.filterNot(_ == monitor)
  }
  def triggerWrittenMonitors(event: PairEvent): Unit = {
    writtenMonitors.filter(_.isDefinedAt(event)).foreach(monitor => invokeLater(monitor(event)))
  }
}
