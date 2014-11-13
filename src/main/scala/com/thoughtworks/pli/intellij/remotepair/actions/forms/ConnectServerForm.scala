package com.thoughtworks.pli.intellij.remotepair.actions.forms

import com.intellij.openapi.ui.ValidationInfo
import scala.util.Try

class ConnectServerForm extends _ConnectServerForm {

  def host = getTxtHost.getText.trim
  def port = getTxtPort.getText.trim

  def host_=(host: String) = getTxtHost.setText(host)
  def port_=(port: String) = getTxtPort.setText(port)

  def validate: Option[ValidationInfo] = (host, port) match {
    case (host, _) if host.trim.isEmpty => Some(new ValidationInfo("server host should not be blank", getTxtHost))
    case (_, port) if !isInteger(port) => Some(new ValidationInfo("server port should be an integer", getTxtPort))
    case (_, port) if port.toInt <= 0 => Some(new ValidationInfo("server port should > 0", getTxtPort))
    case _ => None
  }

  private def isInteger(s: String) = Try(s.toInt).isSuccess

}

