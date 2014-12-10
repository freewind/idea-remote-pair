package com.thoughtworks.pli.intellij.remotepair.statusbar

import java.awt.Component
import java.awt.event.MouseEvent

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem._
import com.intellij.openapi.ui.popup.{JBPopupFactory, ListPopup}
import com.intellij.openapi.wm.StatusBarWidget.{MultipleTextValuesPresentation, PlatformType}
import com.intellij.openapi.wm.{StatusBar, StatusBarWidget}
import com.intellij.util.Consumer
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.actions.{IgnoreFilesAction, StartServerAction, ConnectServerAction}
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.statusbar.PairStatusWidget.{ParallelMode, CaretSharingMode, NotConnect, PairStatus}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

class PairStatusWidget(override val currentProject: RichProject) extends StatusBarWidget with MultipleTextValuesPresentation with CurrentProjectHolder with StatusWidgetPopups with AppLogger {

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
    val group = createActionGroup()
    val dataContext: DataContext = DataManager.getInstance.getDataContext(statusBar.asInstanceOf[Component])
    JBPopupFactory.getInstance.createActionGroupPopup("Remote Pair", group, dataContext, null, false)
  }

  private def createActionGroup(): DefaultActionGroup = {
    val group = new DefaultActionGroup()
    currentProject.context match {
      case Some(_) =>
        group.add(createProjectGroup())
        group.add(createDisconnectAction())
        group.add(createWorkingModeAction())
        group.add(createIgnoreFilesAction())
      case _ =>
        group.add(createConnectServerAction())
    }


    group.addSeparator("pair server")
    currentProject.server match {
      case Some(_) =>
        group.add(createRunningServerGroup())
      case _ =>
        group.add(createStartServerGroup())
    }

    group
  }

  override def getMaxValue = getSelectedValue
  override def getSelectedValue = currentStatus.icon

  override def getTooltipText = currentStatus.tip
  override def getClickConsumer: Consumer[MouseEvent] = new Consumer[MouseEvent] {
    override def consume(t: MouseEvent): Unit = {
      log.info("########### clicked on th status bar: " + t.toString)
    }
  }

  private def setupProjectStatusListener(): Unit = {
    val conn = currentProject.createMessageConnection()
    conn.subscribe(ProjectStatusChanges.ProjectStatusTopic, new ProjectStatusChanges.Listener {
      override def onChange(): Unit = {
        currentStatus = if (currentProject.context.isDefined) {
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

object PairStatusWidget {

  sealed abstract class PairStatus(val icon: String, val tip: String)

  case object NotConnect extends PairStatus("-x-", "not connected yet")

  case object CaretSharingMode extends PairStatus("o-o", "follow others caret changes")

  case object ParallelMode extends PairStatus("o|o", "not follow others caret changes")

}

trait StatusWidgetPopups extends InvokeLater with PublishEvents {
  this: CurrentProjectHolder =>

  def createProjectGroup() = {
    val group = createPopupGroup()
    group.getTemplatePresentation.setText(getCurrentProjectName, false)

    group.addSeparator("Switch to")
    currentProject.projectInfo.map(_.name).foreach { currentProjectName =>
      val otherProjects = currentProject.serverStatus.toList.flatMap(_.projects)
        .map(_.name).filter(_ != currentProjectName)
        .map(createProjectAction)
      group.addAll(otherProjects: _*)
    }

    group.addSeparator("Create new")
    group
  }

  private def getCurrentProjectName = {
    currentProject.projectInfo.map(_.name).getOrElse("No project")
  }

  private def createProjectAction(projectName: String) = {
    new AnAction("???") {
      override def actionPerformed(anActionEvent: AnActionEvent): Unit = {
      }
    }
  }

  def createDisconnectAction() = {
    new AnAction("disconnect") {
      override def actionPerformed(anActionEvent: AnActionEvent): Unit = {
        currentProject.context.foreach(_.close())
      }
    }
  }

  def createIgnoreFilesAction() = new IgnoreFilesAction()

  def createWorkingModeAction() = {
    val group = createPopupGroup()
    group.addSeparator("switch to")
    if (currentProject.projectInfo.exists(_.isCaretSharing)) {
      group.getTemplatePresentation.setText("Caret sharing")
      group.add(new AnAction("Parallel") {
        override def actionPerformed(anActionEvent: AnActionEvent): Unit = {
          publishEvent(ParallelModeRequest)
        }
      })
    } else {
      group.add(new AnAction("Caret sharing") {
        group.getTemplatePresentation.setText("Parallel")
        override def actionPerformed(anActionEvent: AnActionEvent): Unit = {
          publishEvent(CaretSharingModeRequest)
        }
      })
    }
    group
  }

  def createConnectServerAction() = new ConnectServerAction()

  def createRunningServerGroup() = {
    val group = createPopupGroup()
    group.getTemplatePresentation.setText("local server is running")
    group.add(new AnAction("information") {
      override def actionPerformed(anActionEvent: AnActionEvent): Unit = currentProject.showMessageDialog("TODO")
    })
    group.add(new AnAction("stop") {
      override def actionPerformed(anActionEvent: AnActionEvent): Unit = invokeLater {
        currentProject.server.foreach { server =>
          server.close().addListener(new GenericFutureListener[ChannelFuture] {
            override def operationComplete(f: ChannelFuture): Unit = {
              if (f.isSuccess) {
                currentProject.server = None
              } else {
                invokeLater(currentProject.showErrorDialog("Error", "Can't stop server"))
              }
            }
          })
        }
      }
    })
    group
  }

  def createStartServerGroup() = {
    val group = createPopupGroup()
    group.getTemplatePresentation.setText("No local server")
    group.add(new StartServerAction())
    group
  }

  private def createPopupGroup() = new DefaultActionGroup(null, true)
}
