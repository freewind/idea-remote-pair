package com.thoughtworks.pli.intellij.remotepair.actions.forms

import javax.swing._
import com.intellij.openapi.ui.ValidationInfo

class WorkingModeForm extends _WorkingModeForm {

  def setClientsInCaretSharingMode(clients: Seq[String]) = getClientsInCaretSharingMode.setText(clients.mkString(", "))

  def getCaretSharingClients: String = getClientsInCaretSharingMode.getText

  def validate: Option[ValidationInfo] = {
    val radios = getRadioCaretSharingMode :: getRadioParallelMode :: Nil
    if (radios.exists(_.isSelected)) {
      None
    } else {
      Some(new ValidationInfo("Nothing selected", getRadioCaretSharingMode))
    }
  }

  def isParallelMode = this.getRadioParallelMode.isSelected

  def isCaretSharingMode = this.getRadioCaretSharingMode.isSelected

  private def selectedClient(radios: Seq[JRadioButton]) = radios.find(_.isSelected).map(_.getClientProperty("KEY_CLIENT").asInstanceOf[String])

  private def childRadiosOf(panel: JPanel) = panel.getComponents.toList.map(_.asInstanceOf[JRadioButton])

}
