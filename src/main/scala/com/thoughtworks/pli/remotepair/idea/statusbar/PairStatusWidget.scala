package com.thoughtworks.pli.remotepair.idea.statusbar

import java.awt.Component
import java.awt.event.MouseEvent

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem._
import com.intellij.openapi.ui.popup.{JBPopupFactory, ListPopup}
import com.intellij.openapi.wm.StatusBarWidget.{MultipleTextValuesPresentation, PlatformType}
import com.intellij.openapi.wm.{StatusBar, StatusBarWidget}
import com.intellij.util.Consumer
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient
import com.thoughtworks.pli.remotepair.idea.StatusWidgetPopups
import com.thoughtworks.pli.remotepair.idea.idea.CreateMessageConnection
import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidget.{CaretSharingMode, NotConnect, PairStatus, ParallelMode}

import scala.language.existentials

object PairStatusWidget {
  type Factory = () => PairStatusWidget

  sealed abstract class PairStatus(val icon: String, val tip: String)
  case object NotConnect extends PairStatus("not connect", "not connect yet")
  case object CaretSharingMode extends PairStatus("follow carets", "follow others caret changes")
  case object ParallelMode extends PairStatus("parallel", "don't follow others caret changes")
}

class PairStatusWidget(statusWidgetPopups: StatusWidgetPopups, logger: PluginLogger, connectedClient: ConnectedClient, createMessageConnection: CreateMessageConnection)
  extends StatusBarWidget with MultipleTextValuesPresentation {

  private var statusBar: StatusBar = _
  private var currentStatus: PairStatus = NotConnect

  setupProjectStatusListener()

  override def ID() = classOf[PairStatusWidget].getName
  override def install(statusBar: StatusBar): Unit = this.statusBar = statusBar
  override def getPresentation(platformType: PlatformType) = this
  override def dispose(): Unit = {
    statusBar = null
  }

  override def getPopupStep: ListPopup = {
    val group = statusWidgetPopups.createActionGroup()
    val dataContext: DataContext = DataManager.getInstance.getDataContext(statusBar.asInstanceOf[Component])
    JBPopupFactory.getInstance.createActionGroupPopup("Remote Pair", group, dataContext, null, false)
  }


  override def getMaxValue = getSelectedValue
  override def getSelectedValue = "pair" + serverMessage() + masterMessage() + readonlyMessage() + ": " + currentStatus.icon

  private def serverMessage() = if (connectedClient.serverHolder.get.isDefined) " (server)" else ""
  private def masterMessage() = if (connectedClient.amIMaster) " (master)" else ""
  private def readonlyMessage() = if (connectedClient.isReadonlyMode) " (readonly)" else ""


  override def getTooltipText = currentStatus.tip
  override def getClickConsumer: Consumer[MouseEvent] = new Consumer[MouseEvent] {
    override def consume(t: MouseEvent): Unit = {
      logger.info("clicked on th status bar: " + t.toString)
    }
  }

  private def setupProjectStatusListener(): Unit = {
    createMessageConnection().foreach { conn =>
      conn.subscribe(ProjectStatusChanges.ProjectStatusTopic, new ProjectStatusChanges.Listener {
        override def onChange(): Unit = {
          currentStatus = if (connectedClient.isConnected) {
            if (connectedClient.isCaretSharing) {
              CaretSharingMode
            } else {
              ParallelMode
            }
          } else {
            NotConnect
          }
          statusBar.updateWidget(ID())
        }
      })
    }
  }

}
