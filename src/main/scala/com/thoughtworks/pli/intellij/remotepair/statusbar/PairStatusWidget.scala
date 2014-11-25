package com.thoughtworks.pli.intellij.remotepair.statusbar

import java.awt.Component
import java.awt.event.MouseEvent

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, DataContext, DefaultActionGroup}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.{JBPopupFactory, ListPopup}
import com.intellij.openapi.wm.StatusBarWidget.{MultipleTextValuesPresentation, PlatformType}
import com.intellij.openapi.wm.{StatusBar, StatusBarWidget}
import com.intellij.util.Consumer
import com.thoughtworks.pli.intellij.remotepair.statusbar.PairStatusWidget.{NotConnect, PairStatus}

class PairStatusWidget(var project: Project) extends StatusBarWidget with MultipleTextValuesPresentation {

  private var statusBar: StatusBar = _

  private var myText = "start-value"

  override def ID() = classOf[PairStatusWidget].getName
  override def install(statusBar: StatusBar): Unit = this.statusBar = statusBar
  override def getPresentation(platformType: PlatformType) = this
  override def dispose(): Unit = {
    statusBar = null
    project = null
  }

  var currentStatus: PairStatus = NotConnect

  override def getPopupStep: ListPopup = {
    val group = createActionGroup()
    val dataContext: DataContext = DataManager.getInstance.getDataContext(statusBar.asInstanceOf[Component])
    JBPopupFactory.getInstance.createActionGroupPopup("PPPppppppppair", group, dataContext, null, false)
  }

  private def createAction(label: String) = {
    new AnAction(label) {
      override def actionPerformed(anActionEvent: AnActionEvent): Unit = println("Clicked " + label)
    }
  }

  private def createActionGroup(): DefaultActionGroup = {
    new DefaultActionGroup(createAction("action1"), createAction("action2"))
  }

  override def getMaxValue: String = myText
  override def getSelectedValue: String = myText

  override def getTooltipText: String = "This is the pair status widget"
  override def getClickConsumer: Consumer[MouseEvent] = new Consumer[MouseEvent] {
    override def consume(t: MouseEvent): Unit = {
      println("########### clicked on th status bar: " + t.toString)
    }
  }
}

object PairStatusWidget {

  sealed abstract class PairStatus(val icon: String)

  case object NotConnect extends PairStatus("-x-")

  case object CaretSharingMode extends PairStatus("o-o")

  case object ParallelMode extends PairStatus("o|o")

}

