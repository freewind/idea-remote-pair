package com.thoughtworks.pli.intellij.remotepair.actions.forms

import javax.swing._

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

  def getFollowModeRadios: Seq[JRadioButton] = childRadiosOf(this.getFollowModePanel)

  def getSelectedClientNameInFollowMode: Option[String] = selectedClient(getFollowModeRadios)

  def isParallelMode = this.getRadioParallelMode.isSelected

  def isCaretSharingMode = this.getRadioCaretSharingMode.isSelected

  private def selectedClient(radios: Seq[JRadioButton]) = radios.find(_.isSelected).map(_.getClientProperty("KEY_CLIENT").asInstanceOf[String])

  private def childRadiosOf(panel: JPanel) = panel.getComponents.toSeq.map(_.asInstanceOf[JRadioButton])

}
