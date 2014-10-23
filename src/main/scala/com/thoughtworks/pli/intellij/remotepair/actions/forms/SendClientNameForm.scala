package com.thoughtworks.pli.intellij.remotepair.actions.forms

import com.intellij.openapi.ui.ValidationInfo

class SendClientNameForm {

  private val form = new _SendClientNameForm

  def clientNameField = form.getTxtClientName
  def mainPanel = form.getMain

  def clientName_=(value: String) = clientNameField.setText(value)
  def clientName: String = clientNameField.getText.trim

  def validate: Option[ValidationInfo] = Option(clientName).filter(_.isEmpty)
    .map(_ => new ValidationInfo("Client name should not be blank", clientNameField))

}
