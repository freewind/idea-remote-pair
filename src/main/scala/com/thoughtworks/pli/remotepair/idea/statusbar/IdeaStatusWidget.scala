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
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{StatusMenuItem, MyProjectStorage, VirtualIdeStatus}
import com.thoughtworks.pli.remotepair.core.server.MyServer
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import com.thoughtworks.pli.remotepair.idea.models.{IdeaIdeImpl, IdeaProjectImpl}

import scala.language.existentials

object IdeaStatusWidget {
  type Factory = () => IdeaStatusWidget
}

class IdeaStatusWidget(val currentProject: IdeaProjectImpl, val logger: PluginLogger, val myClient: MyClient, val myIde: IdeaIdeImpl, val mySystem: MySystem, val myProjectStorage: MyProjectStorage, val myServer: MyServer, val dialogFactories: DialogFactories)
  extends StatusBarWidget with MultipleTextValuesPresentation with VirtualIdeStatus {

  private var statusBar: Option[StatusBar] = None
  override def ID() = classOf[IdeaStatusWidget].getName
  override def install(statusBar: StatusBar): Unit = this.statusBar = Some(statusBar)
  override def getPresentation(platformType: PlatformType) = this
  override def dispose(): Unit = statusBar = None
  override def getPopupStep: ListPopup = {
    val group = createActionGroup()
    val dataContext: DataContext = DataManager.getInstance.getDataContext(statusBar.get.asInstanceOf[Component])
    JBPopupFactory.getInstance.createActionGroupPopup("Remote Pair", group, dataContext, null, false)
  }
  override def getMaxValue = getSelectedValue
  override def getSelectedValue = statusSummaryText
  override def getTooltipText = currentStatus.tip
  override def getClickConsumer: Consumer[MouseEvent] = new Consumer[MouseEvent] {
    override def consume(t: MouseEvent): Unit = {
      logger.info("clicked on th status bar: " + t.toString)
    }
  }

  currentProject.createMessageConnection().foreach { conn =>
    conn.subscribe(ProjectStatusChanges.ProjectStatusTopic, new ProjectStatusChanges.Listener {
      override def onChange(): Unit = {
        projectStatusChanged()
        statusBar.foreach(_.updateWidget(ID()))
      }
    })
  }

  def createActionGroup() = {
    val group = new DefaultActionGroup()
    if (myClient.isConnected) {
      group.addSeparator("Current project")
      group.add(action(projectInfoItem))
      group.add(action(copyProjectUrlItem))
      group.add(action(watchFilesItem))
      group.add(action(syncFilesItem))
      group.add(action(disconnectItem))

      group.addSeparator("Pair mode")
      group.add(action(followCaretModeItem))
      group.add(action(parallelModeItem))

      group.addSeparator("Readonly")
      group.add(action(readOnlyItem))
    } else {
      group.add(action(connectToServerItem))
    }

    group.addSeparator("Pair server")
    if (myServer.isStarted) {
      group.add(action(serverInfoItem))
      group.add(action(stopServerItem))
    } else {
      group.add(action(startServerItem))
    }

    group
  }

  private def action(item: StatusMenuItem) = new AnAction(item.label) {
    override def actionPerformed(e: AnActionEvent): Unit = item.action()
  }


}
