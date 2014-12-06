package com.thoughtworks.pli.intellij.remotepair.actions.forms

import com.thoughtworks.pli.intellij.MySpecification
import org.specs2.specification.Scope

class WorkingModeFormSpec extends MySpecification {

  "Caret sharing mode" should {
    "allow to set client names to show as hint" in new Mocking {
      form.setClientsInCaretSharingMode(Seq("a1", "a2"))
      form.getClientsInCaretSharingMode.getText ==== "a1, a2"
    }
    "allow to choose" in new Mocking {
      form.getRadioCaretSharingMode.setSelected(true)
      form.isCaretSharingMode === true
    }
  }

  "Parallel mode" should {
    "can be chose" in new Mocking {
      form.getRadioParallelMode.setSelected(true)
      form.isParallelMode === true
    }
  }

  "Validation" should {
    "be invalid if none mode has been chosen" in new Mocking {
      form.validate must beSome.which(_.message === "Nothing selected")
    }
    "be valid if any option of caret sharing mode has been chosen" in new Mocking {
      form.getRadioCaretSharingMode.setSelected(true)
      form.validate === None
    }
    "be valid if any option of parallel mode has been chosen" in new Mocking {
      form.getRadioParallelMode.setSelected(true)
      form.validate === None
    }
  }

  trait Mocking extends Scope {
    val form = new WorkingModeForm
  }

}
