package com.thoughtworks.pli.intellij.remotepair.actions.forms

import com.thoughtworks.pli.intellij.MySpecification
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.DialogMocks
import org.specs2.specification.Scope
import java.io.File
import org.apache.commons.io.FileUtils

class IgnoreFilesFormSpec extends MySpecification {

  "IgnoreFilesForm" should {
    "merge content of .gitignore from project root when clicked on the 'merge' button" in new Mocking {
      form.getFilesContext.setText(".idea\nbbb")
      form.getMergeFromGitIgnoreButton.doClick()
      form.getFilesContext.getText === ".idea\naaa\nbbb"
    }
  }

  trait Mocking extends Scope with DialogMocks {
    val gitIgnore = File.createTempFile("ideaRemotePairTest", ".gitignore")
    FileUtils.write(gitIgnore, "aaa\nbbb")

    val form = new IgnoreFilesForm with MockCurrentProject {
      override def findGitIgnoreFile = Some(gitIgnore)
    }
  }

}
