package com.thoughtworks.pli.remotepair.idea.statusbar

import scala.language.existentials
import java.awt.Component
import java.awt.event.MouseEvent

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem._
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.popup.{JBPopupFactory, ListPopup}
import com.intellij.openapi.wm.StatusBarWidget.{MultipleTextValuesPresentation, PlatformType}
import com.intellij.openapi.wm.{StatusBar, StatusBarWidget}
import com.intellij.util.Consumer
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidget.{CaretSharingMode, NotConnect, PairStatus, ParallelMode}

class PairStatusWidgetFactory(statusWidgetPopups: StatusWidgetPopups, logger: Logger, serverHolder: ServerHolder, amIMaster: AmIMaster, createMessageConnection: CreateMessageConnection, isCaretSharing: IsCaretSharing, connectionHolder: ConnectionHolder) {

  def create() = new StatusBarWidget with MultipleTextValuesPresentation {

    private var statusBar: StatusBar = _

    private var currentStatus: PairStatus = NotConnect

    setupProjectStatusListener()

    override def ID() = classOf[PairStatusWidgetFactory].getName
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
    override def getSelectedValue = "pair" + serverMessage() + masterMessage() + ": " + currentStatus.icon

    private def serverMessage() = if (serverHolder.get.isDefined) " (server)" else ""
    private def masterMessage() = if (amIMaster()) " (master)" else ""

    override def getTooltipText = currentStatus.tip
    override def getClickConsumer: Consumer[MouseEvent] = new Consumer[MouseEvent] {
      override def consume(t: MouseEvent): Unit = {
        logger.info("########### clicked on th status bar: " + t.toString)
      }
    }

    private def setupProjectStatusListener(): Unit = {
      createMessageConnection().foreach { conn =>
        conn.subscribe(ProjectStatusChanges.ProjectStatusTopic, new ProjectStatusChanges.Listener {
          override def onChange(): Unit = {
            currentStatus = if (connectionHolder.get.isDefined) {
              if (!isCaretSharing()) {
                ParallelMode
              } else {
                CaretSharingMode
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

}

object PairStatusWidget {

  sealed abstract class PairStatus(val icon: String, val tip: String)

  case object NotConnect extends PairStatus("not connect", "not connect yet")

  case object CaretSharingMode extends PairStatus("follow carets", "follow others caret changes")

  case object ParallelMode extends PairStatus("parallel", "don't follow others caret changes")

}



