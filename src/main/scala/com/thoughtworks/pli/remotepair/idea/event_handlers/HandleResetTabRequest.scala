package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.ResetTabEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core.PublishEvent
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class HandleResetTabRequest(currentProject: RichProject, invokeLater: InvokeLater, publishEvent: PublishEvent) {
  def apply() {
    // FIXME it can be no opened tab
    invokeLater {
      val path = currentProject.pathOfSelectedTextEditor.getOrElse("")
      publishEvent(ResetTabEvent(path))
    }
  }

}
