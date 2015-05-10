package com.thoughtworks.pli.remotepair.core

import javax.swing.JComponent

import com.intellij.openapi.ui.ValidationInfo
import org.specs2.SpecificationLike

trait ValidationInfoMatcher {
  this: SpecificationLike =>

  def reportError(errorMessage: String, source: JComponent) = beSome.which { info: ValidationInfo =>
    info.message === errorMessage
    info.component === source
  }

}
