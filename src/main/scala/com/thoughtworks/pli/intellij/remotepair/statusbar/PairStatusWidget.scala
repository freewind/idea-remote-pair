package com.thoughtworks.pli.intellij.remotepair.statusbar

import java.awt.event.MouseEvent
import javax.swing.Icon

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget.{IconPresentation, PlatformType}
import com.intellij.openapi.wm.{StatusBar, StatusBarWidget}
import com.intellij.util.Consumer

class PairStatusWidget(var project: Project) extends StatusBarWidget with IconPresentation {

  private var statusBar: StatusBar = _

  override def ID() = classOf[PairStatusWidget].getName
  override def install(statusBar: StatusBar): Unit = this.statusBar = statusBar
  override def getPresentation(platformType: PlatformType) = this
  override def dispose(): Unit = {
    statusBar = null
    project = null
  }


  override def getIcon: Icon = AllIcons.Ide.Warning_notifications
  override def getTooltipText: String = "This is the pair status widget"
  override def getClickConsumer: Consumer[MouseEvent] = new Consumer[MouseEvent] {
    override def consume(t: MouseEvent): Unit = {
      println("########### clicked on th status bar: " + t.toString)
    }
  }
}
