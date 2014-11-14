package com.thoughtworks.pli.intellij.remotepair.actions.forms

import javax.swing._
import com.intellij.openapi.ui.ValidationInfo

class WorkingModeForm extends _WorkingModeForm {

  Seq(this.getCaretSharingModePanel, this.getFollowModePanel).foreach { panel =>
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
  }

  def setClientsInCaretSharingMode(clients: Seq[String]) = getClientsInCaretSharingMode.setText(clients.mkString(", "))

  def getCaretSharingClients: String = getClientsInCaretSharingMode.getText

  def setClientsInFollowMode(followMap: Map[String, Seq[String]]) = {
    followMap.foreach { case (star, fans) =>
      val text = star + " <= " + fans.mkString("(", ",", ")")
      val button = new JRadioButton(text)
      button.putClientProperty("KEY_CLIENT", star)
      getFollowModePanel.add(button)
    }
  }

  def validate: Option[ValidationInfo] = {
    val radios = getRadioCaretSharingMode :: getRadioParallelMode :: getFollowModeRadios
    if (radios.exists(_.isSelected)) {
      None
    } else {
      Some(new ValidationInfo("Nothing selected", getRadioCaretSharingMode))
    }
  }

  def getFollowModeRadios: List[JRadioButton] = childRadiosOf(this.getFollowModePanel)

  def getSelectedClientNameInFollowMode: Option[String] = selectedClient(getFollowModeRadios)

  def isParallelMode = this.getRadioParallelMode.isSelected

  def isCaretSharingMode = this.getRadioCaretSharingMode.isSelected

  private def selectedClient(radios: Seq[JRadioButton]) = radios.find(_.isSelected).map(_.getClientProperty("KEY_CLIENT").asInstanceOf[String])

  private def childRadiosOf(panel: JPanel) = panel.getComponents.toList.map(_.asInstanceOf[JRadioButton])

}
