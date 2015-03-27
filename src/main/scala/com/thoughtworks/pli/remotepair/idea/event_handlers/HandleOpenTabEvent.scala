package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

class HandleOpenTabEvent(getFileByRelative: GetFileByRelative,
                         invokeLater: InvokeLater,
                         publishEvent: PublishEvent,
                         getOpenFileDescriptor: GetOpenFileDescriptor) {

  def apply(path: String) = {
    getFileByRelative(path) match {
      case Some(file) =>
        val openFileDescriptor = getOpenFileDescriptor(file)
        if (openFileDescriptor.canNavigate) {
          invokeLater(openFileDescriptor.navigate(true))
        }
      case _ =>
    }
  }

}
