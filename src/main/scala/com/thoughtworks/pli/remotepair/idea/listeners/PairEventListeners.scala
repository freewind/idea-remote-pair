package com.thoughtworks.pli.remotepair.idea.listeners

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.core.ProjectScopeValue
import com.thoughtworks.pli.remotepair.core.models.{DataKey, MyIde, MyProject}

object PairEventListeners {
  type Monitor = PartialFunction[PairEvent, Any]
}

case class PairEventListeners(myProject: MyProject, myPlatform: MyIde) {

  import PairEventListeners._

  private val readMonitors = new ProjectScopeValue(myProject, new DataKey[Seq[Monitor]](this.getClass.getName + ":KeyReadMonitors"), Nil)
  private val writtenMonitors = new ProjectScopeValue(myProject, new DataKey[Seq[Monitor]](this.getClass.getName + ":KeyWriteMonitors"), Nil)

  def addReadMonitor(monitor: Monitor) = {
    readMonitors.set(readMonitors.get :+ monitor)
  }
  def removeReadMonitor(monitor: Monitor) = {
    readMonitors.set(readMonitors.get.filterNot(_ == monitor))
  }
  def triggerReadMonitors(event: PairEvent): Unit = {
    readMonitors.get.filter(_.isDefinedAt(event)).foreach(monitor => myPlatform.invokeLater(monitor(event)))
  }

  def addWrittenMonitor(monitor: Monitor) = {
    writtenMonitors.set(writtenMonitors.get :+ monitor)
  }
  def removeWrittenMonitor(monitor: Monitor) = {
    writtenMonitors.set(writtenMonitors.get.filterNot(_ == monitor))
  }
  def triggerWrittenMonitors(event: PairEvent): Unit = {
    writtenMonitors.get.filter(_.isDefinedAt(event)).foreach(monitor => myPlatform.invokeLater(monitor(event)))
  }
}
