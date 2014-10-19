package com.thoughtworks.pli.intellij.remotepair.actions.forms

import com.intellij.openapi.ui.ValidationInfo
import scala.util.Try
import javax.swing.{JPanel, JTextField}

class ConnectServerForm {

  private var txtHost: JTextField = null
  private var txtPort: JTextField = null
  private var main: JPanel = null

  def hostField = txtHost
  def portField = txtPort
  def mainPanel = main

  def host = txtHost.getText.trim
  def port = txtPort.getText.trim

  def host_=(host: String) = txtHost.setText(host)
  def port_=(port: String) = txtPort.setText(port)

  def validate: Option[ValidationInfo] = (host, port) match {
    case (host, _) if host.trim.isEmpty => Some(new ValidationInfo("server host should not be blank", txtHost))
    case (_, port) if !isInteger(port) => Some(new ValidationInfo("server port should be an integer", txtPort))
    case (_, port) if port.toInt <= 0 => Some(new ValidationInfo("server port should > 0", txtPort))
    case _ => None
  }

  private def isInteger(s: String) = Try(s.toInt).isSuccess

}

trait ConnectServerFormCreator {
  def createForm() = new ConnectServerForm()
}