package com.thoughtworks.pli.intellij.remotepair.actions.forms

trait ConnectServerFormCreator {
  def createForm() = new ConnectServerForm()
}
