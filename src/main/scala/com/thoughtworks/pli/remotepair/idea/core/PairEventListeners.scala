package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

object PairEventListeners {
  type Monitor = PartialFunction[PairEvent, Any]
  val KeyReadMonitors = new Key[Seq[Monitor]](PairEventListeners.getClass.getName + ":KeyReadMonitors")
  val KeyWriteMonitors = new Key[Seq[Monitor]](PairEventListeners.getClass.getName + ":KeyWriteMonitors")
}

case class PairEventListeners(invokeLater: InvokeLater, currentProjectScope: CurrentProjectScope) {

  import com.thoughtworks.pli.remotepair.idea.core.PairEventListeners._

  private val readMonitors = currentProjectScope.value(KeyReadMonitors, Nil)
  private val writtenMonitors = currentProjectScope.value(KeyWriteMonitors, Nil)

  def addReadMonitor(monitor: Monitor) = {
    readMonitors.set(readMonitors.get :+ monitor)
  }
  def removeReadMonitor(monitor: Monitor) = {
    readMonitors.set(readMonitors.get.filterNot(_ == monitor))
  }
  def triggerReadMonitors(event: PairEvent): Unit = {
    readMonitors.get.filter(_.isDefinedAt(event)).foreach(monitor => invokeLater(monitor(event)))
  }

  def addWrittenMonitor(monitor: Monitor) = {
    writtenMonitors.set(writtenMonitors.get :+ monitor)
  }
  def removeWrittenMonitor(monitor: Monitor) = {
    writtenMonitors.set(writtenMonitors.get.filterNot(_ == monitor))
  }
  def triggerWrittenMonitors(event: PairEvent): Unit = {
    writtenMonitors.get.filter(_.isDefinedAt(event)).foreach(monitor => invokeLater(monitor(event)))
  }
}