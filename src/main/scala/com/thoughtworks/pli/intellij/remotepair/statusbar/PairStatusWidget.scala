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
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.statusbar.PairStatusWidget.{ParallelMode, CaretSharingMode, NotConnect, PairStatus}

class PairStatusWidget(override val currentProject: RichProject) extends StatusBarWidget with MultipleTextValuesPresentation with CurrentProjectHolder with StatusWidgetPopups {

  setupProjectStatusListener()

  private var statusBar: StatusBar = _

  override def ID() = classOf[PairStatusWidget].getName
  override def install(statusBar: StatusBar): Unit = this.statusBar = statusBar
  override def getPresentation(platformType: PlatformType) = this
  override def dispose(): Unit = {
    statusBar = null
  }

  var currentStatus: PairStatus = NotConnect

  override def getPopupStep: ListPopup = {
    val group = createActionGroup()
    val dataContext: DataContext = DataManager.getInstance.getDataContext(statusBar.asInstanceOf[Component])
    JBPopupFactory.getInstance.createActionGroupPopup("Remote Pair", group, dataContext, null, false)
  }

  private def createAction(label: String) = {
    new AnAction(label) {
      override def actionPerformed(anActionEvent: AnActionEvent): Unit = println("Clicked " + label)
    }
  }

  private def createActionGroup(): DefaultActionGroup = {
    val group = new DefaultActionGroup()
    group.add(createProjectGroup())
    group.add(createDisconnectAction())
    group
  }

  override def getMaxValue = getSelectedValue
  override def getSelectedValue = currentStatus.icon

  override def getTooltipText = currentStatus.tip
  override def getClickConsumer: Consumer[MouseEvent] = new Consumer[MouseEvent] {
    override def consume(t: MouseEvent): Unit = {
      println("########### clicked on th status bar: " + t.toString)
    }
  }

  private def setupProjectStatusListener(): Unit = {
    currentProject.createMessageConnection().subscribe(Topics.ProjectStatusTopic, new ProjectStatusChangeListener {
      override def onChange(): Unit = {
        currentStatus = if (currentProject.context.isDefined) {
          if (currentProject.projectInfo.exists(_.workingMode == ParallelModeRequest)) {
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

trait StatusWidgetPopups {
  this: CurrentProjectHolder =>

  def createProjectGroup() = {
    val group = new DefaultActionGroup(null, true)
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

}
