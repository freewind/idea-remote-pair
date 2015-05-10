package com.thoughtworks.pli.remotepair.core.server_event_handlers.editors

import com.thoughtworks.pli.intellij.remotepair.protocol.CloseTabEvent
import com.thoughtworks.pli.remotepair.idea.file.CloseFile
import com.thoughtworks.pli.remotepair.idea.project.GetFileByRelative
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class HandleCloseTabEvent(getFileByRelative: GetFileByRelative,
                               invokeLater: InvokeLater,
                               closeFile: CloseFile) {

  def apply(event: CloseTabEvent) = {
    getFileByRelative(event.path).foreach(file => invokeLater(file.close()))
  }

}
