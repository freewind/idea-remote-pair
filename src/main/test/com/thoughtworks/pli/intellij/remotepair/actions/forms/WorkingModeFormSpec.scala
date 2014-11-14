package com.thoughtworks.pli.intellij.remotepair.actions.forms

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

class WorkingModeFormSpec extends Specification with Mockito {

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

  "Follow mode" should {
    "allow to set client names to show as groups of radios" in new Mocking {
      form.getFollowModeRadios must have length 2
      form.getFollowModeRadios.map(_.getText) === Seq("s1 <= (a1,a2)", "s2 <= (b1,b2)")
    }
    "give a client name of a group if we chose one" in new Mocking {
      form.getFollowModeRadios.head.setSelected(true)
      form.getSelectedClientNameInFollowMode === Some("s1")
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
    "be valid if any option of follow mode has been chosen" in new Mocking {
      form.getFollowModeRadios.head.setSelected(true)
      form.validate === None
    }
    "be valid if any option of parallel mode has been chosen" in new Mocking {
      form.getRadioParallelMode.setSelected(true)
      form.validate === None
    }
  }

  trait Mocking extends Scope {
    val form = new WorkingModeForm
    form.setClientsInFollowMode(Map("s1" -> Seq("a1", "a2"), "s2" -> Seq("b1", "b2")))
  }

}
