package com.thoughtworks.pli.intellij.remotepair.actions.forms

import com.intellij.openapi.ui.ValidationInfo

class SendClientNameForm extends _SendClientNameForm {

  def clientName_=(value: String) = getTxtClientName.setText(value)
  def clientName: String = getTxtClientName.getText.trim

  def validate: Option[ValidationInfo] = Option(clientName).filter(_.isEmpty)
    .map(_ => new ValidationInfo("Client name should not be blank", getTxtClientName))

}
