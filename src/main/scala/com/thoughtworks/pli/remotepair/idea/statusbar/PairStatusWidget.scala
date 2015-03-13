package com.thoughtworks.pli.remotepair.idea.statusbar

import java.awt.Component
import java.awt.event.MouseEvent

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem._
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.popup.{JBPopupFactory, ListPopup}
import com.intellij.openapi.wm.StatusBarWidget.{MultipleTextValuesPresentation, PlatformType}
import com.intellij.openapi.wm.{StatusBar, StatusBarWidget}
import com.intellij.util.Consumer
import com.thoughtworks.pli.intellij.remotepair.protocol.{CaretSharingModeRequest, ParallelModeRequest}
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.remotepair.idea.actions._
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.dialogs._
import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidget.{CaretSharingMode, NotConnect, PairStatus, ParallelMode}
import com.thoughtworks.pli.remotepair.idea.utils.{GetLocalIp, InvokeLater}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener
import com.thoughtworks.pli.remotepair.idea._

object PairStatusWidgetFactory {
  type PairStatusWidget = PairStatusWidgetFactory#create
}

case class PairStatusWidgetFactory(currentProject: RichProject, statusWidgetPopups: StatusWidgetPopups, logger: Logger) {
  println("######################### PairStatusWidgetFactory created: " + currentProject)

  case class create() extends StatusBarWidget with MultipleTextValuesPresentation {

    println("######################### PairStatusWidgetFactory.create created: " + currentProject)

    private var statusBar: StatusBar = _

    private var currentStatus: PairStatus = NotConnect

    setupProjectStatusListener()

    override def ID() = classOf[create].getName
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

    private def serverMessage() = if (currentProject.server.isDefined) " (server)" else ""
    private def masterMessage() = if (statusWidgetPopups.isMaster) " (master)" else ""

    override def getTooltipText = currentStatus.tip
    override def getClickConsumer: Consumer[MouseEvent] = new Consumer[MouseEvent] {
      override def consume(t: MouseEvent): Unit = {
        logger.info("########### clicked on th status bar: " + t.toString)
      }
    }

    private def setupProjectStatusListener(): Unit = {
      if (currentProject == null) {
        throw new IllegalStateException("currentProject is null")
      }
      currentProject
        .createMessageConnection()
        .foreach {
        conn =>
          conn.subscribe(ProjectStatusChanges.ProjectStatusTopic, new ProjectStatusChanges.Listener {
            override def onChange(): Unit = {
              currentStatus = if (currentProject.connection.isDefined) {
                if (!currentProject.projectInfo.exists(_.isCaretSharing)) {
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

case class StatusWidgetPopups(currentProject: RichProject, invokeLater: InvokeLater, publishEvent: PublishEvent, localIp: GetLocalIp,
                         syncFilesForMasterDialogFactory: SyncFilesForMasterDialogFactory, syncFilesForSlaveDialogFactory: SyncFilesForSlaveDialogFactory) {

  import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidget._

  def createActionGroup(): DefaultActionGroup = {
    val group = new DefaultActionGroup()
    currentProject.connection match {
      case Some(_) =>
        group.addSeparator("Current project")
        createProjectName().foreach(group.add)
        group.add(new IgnoreFilesAction())
        group.add(action("Sync files", createSyncDialog().showOnCenter()))
        group.add(action("Disconnect", currentProject.connection.foreach(_.close())))

        group.addSeparator("Pair mode")
        group.addAll(createPairModeGroup(): _*)
      case _ =>
        group.add(createConnectServerAction())
    }

    group.addSeparator("Pair server")
    currentProject.server match {
      case Some(server) =>
        group.add(createRunningServerGroup(server))
      case _ =>
        group.add(createStartServerAction())
    }

    group
  }

  def createSyncDialog(): JDialogSupport = {
    if (isMaster) {
      syncFilesForMasterDialogFactory.create()
    } else {
      syncFilesForSlaveDialogFactory.create()
    }
  }

  def createProjectName() = for {
    projectName <- currentProject.projectInfo.map(_.name)
    clients <- currentProject.projectInfo.map(_.clients)
    names = clients.map(_.name)
  } yield chosenAction(s"$projectName (${names.mkString(",")})")

  private def chosenAction(label: String) = new AnAction("√ " + label) {
    override def actionPerformed(anActionEvent: AnActionEvent): Unit = ()
  }

  def action(label: String, f: => Any) = new AnAction(label) {
    override def actionPerformed(anActionEvent: AnActionEvent): Unit = f
  }

  def createPairModeGroup(): Seq[AnAction] = if (currentProject.projectInfo.exists(_.isCaretSharing)) {
    Seq(chosenAction(CaretSharingMode.icon),
      action(ParallelMode.icon, publishEvent(ParallelModeRequest)))
  } else {
    Seq(action(CaretSharingMode.icon, publishEvent(CaretSharingModeRequest)),
      chosenAction(ParallelMode.icon))
  }

  def createConnectServerAction() = new ConnectServerAction()

  def createRunningServerGroup(server: Server) = {
    val group = createPopupGroup()
    group.getTemplatePresentation.setText(s"Local server => ${server.host.getOrElse(localIp())}:${server.port}")
    group.add(action("stop", invokeLater {
      server.close().addListener(new GenericFutureListener[ChannelFuture] {
        override def operationComplete(f: ChannelFuture): Unit = {
          if (f.isSuccess) {
            currentProject.server = None
          } else {
            invokeLater(currentProject.showErrorDialog("Error", "Can't stop server"))
          }
        }
      })
    }))
    group
  }

  def createStartServerAction() = new StartServerAction()

  private def createPopupGroup() = new DefaultActionGroup(null, true)

  def isMaster = currentProject.clientInfo.exists(_.isMaster)
}

