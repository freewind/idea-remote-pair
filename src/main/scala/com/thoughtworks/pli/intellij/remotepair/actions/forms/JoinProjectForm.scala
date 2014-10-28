package com.thoughtworks.pli.intellij.remotepair.actions.forms

import javax.swing._
import java.awt.event._
import com.intellij.openapi.ui.ValidationInfo

class JoinProjectForm {
  private val form = new _JoinProjectForm

  form.getRadioNewProject.addItemListener(new ItemListener {
    def itemStateChanged(event: ItemEvent) {
      if (event.getStateChange == ItemEvent.SELECTED) {
        existingProjectRadios.foreach(_.setEnabled(false))
        newProjectTextField.setEnabled(true)
      } else {
        existingProjectRadios.foreach(_.setEnabled(true))
        newProjectTextField.setEnabled(false)
      }
    }
  })

  def validate(): Option[ValidationInfo] = Some(new ValidationInfo("sdfdsf"))

  def getNewProjectName: Option[String] = if (newProjectField.isSelected) newProjectName else None

  def getExistingProjectName: Option[String] = existingProjectRadios.find(_.isSelected).map(_.getText)
  
  def setExistingProjects(projects: Seq[String]) = {
    projects.foreach(p => form.getExistingProjectPanel.add(new JRadioButton(p)))
  }

  def existingProjectRadios: Seq[JRadioButton] = {
    form.getExistingProjectPanel.getComponents.toSeq.map(_.asInstanceOf[JRadioButton])
  }

  def selectedExistingProject: Option[String] = {
    existingProjectRadios.find(_.isSelected).map(_.getText)
  }

  def newProjectTextField = form.getTxtNewProjectName

  def newProjectName: Option[String] = Some(newProjectTextField.getText).filterNot(_.isEmpty).map(_.trim)

  def newProjectField = form.getRadioNewProject

  def mainPanel = form.getMainPanel
}
