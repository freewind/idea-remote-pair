package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing._

import com.thoughtworks.pli.remotepair.core.models.MyProject

trait JDialogSupport {
  this: JDialog =>

  def currentProject: MyProject

  case class Size(width: Int, height: Int)

  private var size: Option[Size] = None

  setModal(true)
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  def setSize(size: Size) = this.size = Some(size)

  def showOnCenter(): Unit = {
    this.pack()
    this.size.foreach(s => setSize(s.width, s.height))
    this.setLocationRelativeTo(currentProject.window)
    this.setVisible(true)
  }

}
