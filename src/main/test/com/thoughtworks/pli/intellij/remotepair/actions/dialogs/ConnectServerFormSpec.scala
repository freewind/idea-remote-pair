package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import org.specs2._
import org.specs2.specification.Scope
import org.specs2.matcher.ThrownExpectations

class ConnectServerFormSpec extends Specification with ThrownExpectations {
  def is = s2"""

# ConnectServerForm

ConnectServerForm is used to input some server information by user, so user
can connect server with them.

## Initial values

ConnectServerForm can be initialized with some values,
which will be displayed immediately when the dialog is opened,
so user can type less and have a good default values

e.g.

  1. the target server ip $e1
  2. the server port $e2
  3. the client name $e3

## Show message

It also contains a label that can display message (e.g. login failed) to the user.

- we can set a custom message and get it. $e4

"""

  def e1 = new Mocking {
    form.init("1.1.1.1", 0, "any")
    form.getHost === "1.1.1.1"
  }
  def e2 = new Mocking {
    form.init("any", 123, "any")
    form.getPort === "123"
  }
  def e3 = new Mocking {
    form.init("any", 0, "Freewind")
    form.getClientName === "Freewind"
  }
  def e4 = new Mocking {
    form.setMessage("Hello")
    form.getMessage === "Hello"
  }

  trait Mocking extends Scope {
    val form = new ConnectServerForm
  }

}
