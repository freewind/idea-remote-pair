package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.core.client.{InWatchingList, PublishEvent}
import com.thoughtworks.pli.remotepair.core.{IsReadonlyMode, PluginLogger}
import com.thoughtworks.pli.remotepair.idea.editor.GetSelectionEventInfo

class HandleSelectionEvent(publishEvent: PublishEvent, logger: PluginLogger, inWatchingList: InWatchingList, getSelectionEventInfo: GetSelectionEventInfo, isReadonlyMode: IsReadonlyMode) {
  def apply(event: EditorSelectionChangeEvent): Unit = if (inWatchingList(event.file) && !isReadonlyMode()) {
    for {
      path <- event.file.relativePath
      ee = SelectContentEvent(path, event.startOffset, event.length)
    } publishEvent(ee)
  }
}
