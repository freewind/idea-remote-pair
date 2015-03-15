package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.ResetTabEvent
import com.thoughtworks.pli.remotepair.idea.core.{GetSelectedTextEditor, GetEditorPath, PublishEvent}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class HandleResetTabRequest(getSelectedTextEditor: GetSelectedTextEditor, getEditorPath: GetEditorPath, invokeLater: InvokeLater, publishEvent: PublishEvent) {
  def apply() {
    // FIXME it can be no opened tab
    invokeLater {
      getSelectedTextEditor().foreach { editor =>
        val path = getEditorPath(editor).getOrElse("")
        publishEvent(ResetTabEvent(path))
      }
    }
  }

}
