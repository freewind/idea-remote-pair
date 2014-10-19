package com.thoughtworks.pli.intellij.remotepair.actions.forms

import com.intellij.openapi.ui.ValidationInfo
import scala.util.Try

class ConnectServerForm {
  private val form = new _ConnectServerForm

  def hostField = form.getTxtHost
  def portField = form.getTxtPort
  def mainPanel = form.getMain

  def host = hostField.getText.trim
  def port = portField.getText.trim

  def host_=(host: String) = hostField.setText(host)
  def port_=(port: String) = portField.setText(port)

  def validate: Option[ValidationInfo] = (host, port) match {
    case (host, _) if host.trim.isEmpty => Some(new ValidationInfo("server host should not be blank", hostField))
    case (_, port) if !isInteger(port) => Some(new ValidationInfo("server port should be an integer", portField))
    case (_, port) if port.toInt <= 0 => Some(new ValidationInfo("server port should > 0", portField))
    case _ => None
  }

  private def isInteger(s: String) = Try(s.toInt).isSuccess

}

