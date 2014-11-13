package com.thoughtworks.pli.intellij.remotepair.actions.forms

import javax.swing._
import java.awt.event._
import com.intellij.openapi.ui.ValidationInfo

class JoinProjectForm extends _JoinProjectForm {

  this.getRadioNewProject.addItemListener(new ItemListener {
    def itemStateChanged(event: ItemEvent) {
      if (event.getStateChange == ItemEvent.SELECTED) {
        existingProjectRadios.foreach(_.setEnabled(false))
        getTxtNewProjectName.setEnabled(true)
      } else {
        existingProjectRadios.foreach(_.setEnabled(true))
        getTxtNewProjectName.setEnabled(false)
      }
    }
  })

  def validate(): Option[ValidationInfo] = Some(new ValidationInfo("sdfdsf"))

  def getNewProjectName: Option[String] = if (getRadioNewProject.isSelected) newProjectName else None

  def getExistingProjectName: Option[String] = existingProjectRadios.find(_.isSelected).map(_.getText)
  
  def setExistingProjects(projects: Seq[String]) = {
    projects.foreach(p => this.getExistingProjectPanel.add(new JRadioButton(p)))
  }

  def existingProjectRadios: Seq[JRadioButton] = {
    this.getExistingProjectPanel.getComponents.toSeq.map(_.asInstanceOf[JRadioButton])
  }

  def selectedExistingProject: Option[String] = {
    existingProjectRadios.find(_.isSelected).map(_.getText)
  }

  def newProjectName: Option[String] = Some(getTxtNewProjectName.getText).filterNot(_.isEmpty).map(_.trim)

}
