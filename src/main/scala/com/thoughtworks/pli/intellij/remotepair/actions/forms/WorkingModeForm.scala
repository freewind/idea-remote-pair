package com.thoughtworks.pli.intellij.remotepair.actions.forms

import javax.swing._

class WorkingModeForm extends _WorkingModeForm {

  Seq(this.getCaretSharingModePanel, this.getFollowModePanel).foreach { panel =>
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
  }

  def setClientsInCaretSharingMode(clients: Seq[Seq[String]]) = addRadios(this.getCaretSharingModePanel, clients)

  def getCaretSharingModeRadios: Seq[JRadioButton] = childRadiosOf(this.getCaretSharingModePanel)

  def setClientsInFollowMode(clients: Seq[Seq[String]]) = addRadios(this.getFollowModePanel, clients)

  def getFollowModeRadios: Seq[JRadioButton] = childRadiosOf(this.getFollowModePanel)

  def getSelectedClientNameInFollowMode: Option[String] = selectedClient(getFollowModeRadios)

  def getSelectedClientNameInCaretSharingMode: Option[String] = selectedClient(getCaretSharingModeRadios)

  def isParallelMode = this.getParallelModeRadio.isSelected

  private def selectedClient(radios: Seq[JRadioButton]) = radios.find(_.isSelected).map(_.getClientProperty("KEY_CLIENT").asInstanceOf[String])

  private def childRadiosOf(panel: JPanel) = panel.getComponents.toSeq.map(_.asInstanceOf[JRadioButton])

  private def addRadios(panel: JPanel, clients: Seq[Seq[String]]) = clients.foreach { names =>
    val text = names.mkString("(", ",", ")")
    val button = new JRadioButton(text)
    button.putClientProperty("KEY_CLIENT", names.head)
    panel.add(button)
  }
}
