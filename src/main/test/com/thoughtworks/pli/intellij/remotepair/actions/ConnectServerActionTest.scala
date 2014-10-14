package com.thoughtworks.pli.intellij.remotepair.actions

import org.specs2.mutable.Specification

class ConnectServerActionTest extends Specification {

  "Connecting Dialog" should {
    "show when run action" in todo
    "have a target server url input field with empty value" in todo
    "have a target server port input field with default value from application setting" in todo
    "show local ip" in todo
    "have a client name input field with default value of local host name" in todo
    "have a 'connect' button" in todo
    "have a 'close' button" in todo
  }

  "Close button" should {
    "close the dialog when click" in todo
  }

  "Connect button" should {
    "save target server url value when clicked" in todo
    "save target server port value when clicked" in todo
    "save client name when clicked" in todo
    "connect server when clicked" in todo
  }

  "Error dialog" should {
    "be shown when port is not number" in todo
    "be shown if connecting is failed" in todo
  }

}
