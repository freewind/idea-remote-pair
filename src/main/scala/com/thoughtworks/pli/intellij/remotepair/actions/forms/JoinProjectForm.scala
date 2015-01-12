package com.thoughtworks.pli.intellij.remotepair.actions.forms

import java.awt.Component
import javax.swing._
import javax.swing.event.{ChangeEvent, ChangeListener}

import com.intellij.openapi.ui.ValidationInfo

class JoinProjectForm extends _JoinProjectForm {

  val radioGroup = new ButtonGroup
  radioGroup.add(this.getRadioNewProject)

  this.getRadioNewProject.addChangeListener(new ChangeListener {
    override def stateChanged(changeEvent: ChangeEvent): Unit = {
      getTxtNewProjectName.setEnabled(getRadioNewProject.isSelected)
    }
  })

  def validate(): Option[ValidationInfo] = None // FIXME

  def getNewProjectName: Option[String] = if (getRadioNewProject.isSelected) newProjectName else None

  def getExistingProjectName: Option[String] = existingProjectRadios.find(_.isSelected).map(_.getText)

  def setExistingProjects(projects: Seq[ProjectWithMemberNames]): Unit = {
    def newProjectPanel(p: ProjectWithMemberNames) = {
      val radio = new JRadioButton(p.projectName)

      val panel = new JPanel()
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS))
      panel.add(radio)
      panel.add(new JLabel(p.memberNames.mkString(" : ", ",", "")))
      panel.setAlignmentX(Component.LEFT_ALIGNMENT)
      (panel, radio)
    }
    this.getExistingProjectPanel.setLayout(new BoxLayout(getExistingProjectPanel, BoxLayout.Y_AXIS))

    for {
      (panel, radio) <- projects.map(newProjectPanel)
    } {
      getExistingProjectPanel.add(panel)
      radioGroup.add(radio)
    }
  }

  def existingProjectRadios: Seq[JRadioButton] = {
    def getRadioFromPanel(panel: JPanel) = panel.getComponent(0).asInstanceOf[JRadioButton]
    this.getExistingProjectPanel.getComponents.toSeq.map(panel => getRadioFromPanel(panel.asInstanceOf[JPanel]))
  }

  def selectedExistingProject: Option[String] = {
    existingProjectRadios.find(_.isSelected).map(_.getText)
  }

  def newProjectName: Option[String] = Some(getTxtNewProjectName.getText).filterNot(_.isEmpty).map(_.trim)

  def clientName: String = txtClientName.getText

  def clientName_=(name: String) = txtClientName.setText(name)

  def showPreErrorMessage(message: String) = lblPreErrorMessage.setText(s"Error: $message")

  def hidePreErrorMessage() = lblPreErrorMessage.setVisible(false)
}

case class ProjectWithMemberNames(projectName: String, memberNames: Seq[String])
