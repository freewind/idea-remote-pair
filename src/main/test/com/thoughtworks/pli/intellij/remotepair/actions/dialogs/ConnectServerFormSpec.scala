package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import org.specs2._
import org.specs2.specification.Scope
import org.specs2.matcher.ThrownExpectations
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.JComponent
import com.thoughtworks.pli.intellij.remotepair.actions.forms.ConnectServerForm

class ConnectServerFormSpec extends Specification with ThrownExpectations {
  def is = s2"""

# ConnectServerForm

ConnectServerForm is used to input some server information by user, so user
can connect server with them.

## Fields

There are some fields user can input and get their values, and their value should be trimmed:

  1. the target server ip $e1
  2. the server port $e2

## Validation

If any field contains invalid value, the "validate" method will return message and the field.

e.g.

- "server host" is empty. $e3
- "server port" is empty. $e4
- "server port" is not integer. $e5
- "server port" is = 0. $e6
- "server port" is < 0. $e7

"""

  private def e1 = new Mocking {
    form.host = "  1.1.1.1  "
    form.host === "1.1.1.1"
  }

  private def e2 = new Mocking {
    form.port = "  123  "
    form.port === "123"
  }

  private def e3 = new Mocking {
    form.host = "     "
    form.validate must reportError("server host should not be blank", form.hostField)
  }

  private def e4 = new Mocking {
    form.port = "  "
    form.validate must reportError("server port should be an integer", form.portField)
  }

  private def e5 = new Mocking {
    form.port = "1.1"
    form.validate must reportError("server port should be an integer", form.portField)
  }

  private def e6 = new Mocking {
    form.port = "0"
    form.validate must reportError("server port should > 0", form.portField)
  }

  private def e7 = new Mocking {
    form.port = "-1"
    form.validate must reportError("server port should > 0", form.portField)
  }

  def reportError(errorMessage: String, source: JComponent) = beSome.which { info: ValidationInfo =>
    info.message === errorMessage
    info.component === source
  }

  trait Mocking extends Scope {
    val form = new ConnectServerForm()
    form.host = "test-host"
    form.port = "9999"
  }

}
