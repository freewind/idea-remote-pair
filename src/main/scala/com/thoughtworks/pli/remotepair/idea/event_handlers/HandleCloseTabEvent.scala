package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.CloseTabEvent
import com.thoughtworks.pli.remotepair.idea.core.{CloseFile, GetFileByRelative}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

case class HandleCloseTabEvent(getFileByRelative: GetFileByRelative,
                               invokeLater: InvokeLater,
                               closeFile: CloseFile) {

  def apply(event: CloseTabEvent) = {
    getFileByRelative(event.path).foreach(file => invokeLater(closeFile(file)))
  }

}
