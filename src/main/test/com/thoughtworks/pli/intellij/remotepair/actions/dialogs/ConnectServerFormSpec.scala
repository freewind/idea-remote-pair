package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ConnectServerFormSpec extends Specification {

  "ConnectServerForm" should {
    "be initialized with target server ip" in new Mocking {
      form.init("1.1.1.1", 0, "any")
      form.getIp === "1.1.1.1"
    }
    "be initialized with target server port" in new Mocking {
      form.init("any", 123, "any")
      form.getPort === 123
    }
    "be initialized with client name" in new Mocking {
      form.init("any", 0, "Freewind")
      form.getUsername === "Freewind"
    }
  }

  trait Mocking extends Scope {
    val form = new ConnectServerForm
  }

}
