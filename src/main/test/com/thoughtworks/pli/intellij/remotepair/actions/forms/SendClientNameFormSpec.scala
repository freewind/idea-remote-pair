package com.thoughtworks.pli.intellij.remotepair.actions.forms

import org.specs2.Specification
import org.specs2.matcher.ThrownExpectations
import org.specs2.specification.{Scope, Fragments}
import com.thoughtworks.pli.intellij.remotepair.ValidationInfoMatcher

class SendClientNameFormSpec extends Specification with ValidationInfoMatcher with ThrownExpectations {
  override def is: Fragments = s2"""

# SendClientNameForm

SendClientNameForm is used to input client name and send to server after connecting server.

## Fields

It has a text filed to let user input client name:

- we can set and get the "client name" text . $e1
- We should get trimmed "client name". $e2

## Validation

- it's invalid if the "client name" is blank. $e3

"""

  private def e1 = new Mocking {
    form.clientName = "Freewind"
    form.clientName === "Freewind"
  }

  private def e2 = new Mocking {
    form.clientName = "  Freewind  "
    form.clientName === "Freewind"
  }

  private def e3 = new Mocking {
    form.clientName = "    "
    form.validate must reportError("Client name should not be blank", form.clientNameField)
  }

  trait Mocking extends Scope {
    val form = new SendClientNameForm
  }

}
