package com.thoughtworks.pli.intellij.remotepair.actions.forms

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

class WorkingModeFormSpec extends Specification with Mockito {

  "Caret sharing mode" should {
    "allow to set client names to show as groups of radios" in new Mocking {
      form.setClientsInCaretSharingMode(Seq(Seq("a1", "a2"), Seq("b1", "b2")))
      form.getCaretSharingModeRadios must have length 2
      form.getCaretSharingModeRadios.map(_.getText) === Seq("(a1,a2)", "(b1,b2)")
    }
    "give a client name of a group if we chose one" in new Mocking {
      form.setClientsInCaretSharingMode(Seq(Seq("a1", "a2")))
      form.getCaretSharingModeRadios.head.setSelected(true)
      form.getSelectedClientNameInCaretSharingMode === Some("a1")
    }
  }

  "Follow mode" should {
    "allow to set client names to show as groups of radios" in new Mocking {
      form.setClientsInFollowMode(Seq(Seq("a1", "a2"), Seq("b1", "b2")))
      form.getFollowModeRadios must have length 2
      form.getFollowModeRadios.map(_.getText) === Seq("(a1,a2)", "(b1,b2)")
    }
    "give a client name of a group if we chose one" in new Mocking {
      form.setClientsInFollowMode(Seq(Seq("a1", "a2"), Seq("b1", "b2")))
      form.getFollowModeRadios.head.setSelected(true)
      form.getSelectedClientNameInFollowMode === Some("a1")
    }
  }

  "Parallel mode" should {
    "can be chose" in new Mocking {
      form.getParallelModeRadio.setSelected(true)
      form.isParallelMode === true
    }
  }

  "Validation" should {
    "be invalid if none mode has been chosen" in todo
    "be valid if any option of caret sharing mode has been chosen" in todo
    "be valid if any option of follow mode has been chosen" in todo
    "be valid if any option of parallel mode has been chosen" in todo
  }

  trait Mocking extends Scope {
    val form = new WorkingModeForm
  }

}
