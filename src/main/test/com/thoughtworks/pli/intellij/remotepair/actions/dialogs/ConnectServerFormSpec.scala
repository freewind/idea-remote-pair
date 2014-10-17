package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import org.specs2._
import org.specs2.specification.Scope

class ConnectServerFormSpec extends Specification { def is = s2"""

ConnectServerForm can be initialized with some values,
which will be displayed immediately when the dialog is opened,
so user can type less and have a good default values

e.g.

  1. the target server ip $e1
  2. the server port $e2
  3. the client name $e3

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

  trait Mocking extends Scope {
    val form = new ConnectServerForm
  }

}
