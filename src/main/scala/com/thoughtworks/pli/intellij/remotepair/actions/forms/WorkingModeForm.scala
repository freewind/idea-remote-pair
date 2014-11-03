package com.thoughtworks.pli.intellij.remotepair.actions.forms

import javax.swing._

class WorkingModeForm {

  private val form = new _WorkingModeForm

  Seq(form.getCaretSharingModePanel, form.getFollowModePanel).foreach { panel =>
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
  }

  def setClientsInCaretSharingMode(clients: Seq[Seq[String]]) = addRadios(form.getCaretSharingModePanel, clients)

  def getCaretSharingModeRadios: Seq[JRadioButton] = childRadiosOf(form.getCaretSharingModePanel)

  def setClientsInFollowMode(clients: Seq[Seq[String]]) = addRadios(form.getFollowModePanel, clients)

  def getFollowModeRadios: Seq[JRadioButton] = childRadiosOf(form.getFollowModePanel)

  def getSelectedClientNameInFollowMode: Option[String] = selectedClient(getFollowModeRadios)

  def getSelectedClientNameInCaretSharingMode: Option[String] = selectedClient(getCaretSharingModeRadios)

  def getParallelModeRadio = form.getParallelModeRadio

  def isParallelMode = form.getParallelModeRadio.isSelected

  def mainPanel = form.getMainPanel

  private def selectedClient(radios: Seq[JRadioButton]) = radios.find(_.isSelected).map(_.getClientProperty("KEY_CLIENT").asInstanceOf[String])

  private def childRadiosOf(panel: JPanel) = panel.getComponents.toSeq.map(_.asInstanceOf[JRadioButton])

  private def addRadios(panel: JPanel, clients: Seq[Seq[String]]) = clients.foreach { names =>
    val text = names.mkString("(", ",", ")")
    val button = new JRadioButton(text)
    button.putClientProperty("KEY_CLIENT", names.head)
    panel.add(button)
  }
}
