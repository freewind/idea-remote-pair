package com.thoughtworks.pli.remotepair.idea.statusbar

import java.awt.Component
import java.awt.event.MouseEvent

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem._
import com.intellij.openapi.ui.popup.{JBPopupFactory, ListPopup}
import com.intellij.openapi.wm.StatusBarWidget.{MultipleTextValuesPresentation, PlatformType}
import com.intellij.openapi.wm.{StatusBar, StatusBarWidget}
import com.intellij.util.Consumer
import com.thoughtworks.pli.intellij.remotepair.protocol.{CaretSharingModeRequest, ParallelModeRequest}
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.server.MyServer
import com.thoughtworks.pli.remotepair.core.ui.DialogFactories
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.language.existentials

object IdeaStatusBarWidget {
  type Factory = () => IdeaStatusBarWidget
}

class IdeaStatusBarWidget(val currentProject: MyProject, val logger: PluginLogger, val myClient: MyClient, val myIde: MyIde, val mySystem: MySystem, val myProjectStorage: MyProjectStorage, val myServer: MyServer, val dialogFactories: DialogFactories)
  extends StatusBarWidget with MultipleTextValuesPresentation {

  private var statusBar: Option[StatusBar] = None
  override def ID() = classOf[IdeaStatusBarWidget].getName
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

  protected var currentStatus: PairStatus = NotConnect

  val projectInfoItem = new StatusMenuItem {
    override def label: String = {
      val names = myClient.allClients.map(_.name)
      s"Members (${names.mkString(",")})"
    }
    override def action(): Unit = ()
  }

  val copyProjectUrlItem = new StatusMenuItem {
    override def label: String = "Copy project url to clipboard"
    override def action(): Unit = {
      myProjectStorage.projectUrl match {
        case Some(url) => mySystem.copyToClipboard(url)
        case _ =>
      }
    }
  }

  val disconnectItem = new StatusMenuItem {
    def label = "Disconnect"
    def action() = myClient.closeConnection()
  }

  val syncFilesItem = new StatusMenuItem {
    override def label: String = "Sync files"
    override def action(): Unit = {
      val dialog = if (myClient.amIMaster) {
        dialogFactories.createSyncFilesForMasterDialog
      } else {
        dialogFactories.createSyncFilesForSlaveDialog
      }
      dialog.showOnCenter()
    }
  }

  val watchFilesItem = new StatusMenuItem {
    override def label: String = "Show watching files"
    override def action(): Unit = dialogFactories.createWatchFilesDialog(None).showOnCenter()
  }

  val readOnlyItem = new StatusMenuItem {
    override def label: String = (if (myClient.isReadonlyMode) "√ " else "") + "readonly"
    override def action(): Unit = {
      myClient.setReadonlyMode(readonly = !myClient.isReadonlyMode)
    }
  }

  val followCaretModeItem = new StatusMenuItem {
    override def label: String = (if (myClient.isCaretSharing) "√ " else "") + CaretSharingMode.icon
    override def action(): Unit = if (!myClient.isCaretSharing) myClient.publishEvent(CaretSharingModeRequest)
  }

  val parallelModeItem = new StatusMenuItem {
    override def label: String = (if (myClient.isCaretSharing) "" else "√ ") + ParallelMode.icon
    override def action(): Unit = if (myClient.isCaretSharing) myClient.publishEvent(ParallelModeRequest)
  }

  val connectToServerItem = new StatusMenuItem {
    override def label: String = "Connect to server"
    override def action(): Unit = dialogFactories.createConnectServerDialog.showOnCenter()
  }

  val startServerItem = new StatusMenuItem {
    override def label: String = "Start local server"
    override def action(): Unit = myServer.start(DefaultValues.DefaultServerPort)
  }

  val serverInfoItem = new StatusMenuItem {
    override def label: String = myServer.serverHolder.get
      .map(server => s"Local server => ${server.host.getOrElse(mySystem.localIp)}:${server.port}")
      .getOrElse("No local server")
    override def action(): Unit = ()
  }

  val stopServerItem = new StatusMenuItem {
    override def label: String = "stop server"
    override def action(): Unit = myServer.serverHolder.get.foreach { server =>
      myIde.invokeLater {
        server.close().addListener(new GenericFutureListener[ChannelFuture] {
          override def operationComplete(f: ChannelFuture): Unit = {
            if (f.isSuccess) {
              myServer.serverHolder.set(None)
            } else {
              myIde.invokeLater(currentProject.showErrorDialog("Error", "Can't stop server"))
            }
          }
        })
      }
    }
  }

  def statusSummaryText: String = "pair" + serverMessage() + masterMessage() + readonlyMessage() + ": " + currentStatus.icon
  private def serverMessage() = if (myServer.isStarted) " (server)" else ""
  private def masterMessage() = if (myClient.amIMaster) " (master)" else ""
  private def readonlyMessage() = if (myClient.isReadonlyMode) " (readonly)" else ""

  def projectStatusChanged(): Unit = {
    currentStatus = (myClient.isConnected, myClient.isCaretSharing) match {
      case (true, true) => CaretSharingMode
      case (true, false) => ParallelMode
      case _ => NotConnect
    }
  }

}

sealed abstract class PairStatus(val icon: String, val tip: String)
case object NotConnect extends PairStatus("not connect", "not connect yet")
case object CaretSharingMode extends PairStatus("follow carets", "follow others caret changes")
case object ParallelMode extends PairStatus("parallel", "don't follow others caret changes")

trait StatusMenuItem {
  def label: String
  def action(): Unit
}
