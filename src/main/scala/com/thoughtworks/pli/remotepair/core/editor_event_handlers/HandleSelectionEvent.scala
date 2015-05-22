package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient
import com.thoughtworks.pli.remotepair.idea.editor.GetSelectionEventInfo

class HandleSelectionEvent(connectedClient: ConnectedClient, logger: PluginLogger, getSelectionEventInfo: GetSelectionEventInfo) {
  def apply(event: EditorSelectionChangeEvent): Unit = if (connectedClient.isWatching(event.file) && !connectedClient.isReadonlyMode) {
    for {
      path <- event.file.relativePath
      ee = SelectContentEvent(path, event.startOffset, event.length)
    } connectedClient.publishEvent(ee)
  }
}
