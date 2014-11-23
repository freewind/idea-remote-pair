package com.thoughtworks.pli.intellij.remotepair.client

import com.thoughtworks.pli.intellij.remotepair.RichProject

trait CurrentProjectHolder {
  val currentProject: RichProject
}
