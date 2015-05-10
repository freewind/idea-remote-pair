package com.thoughtworks.pli.remotepair.core.idea_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.core.{IsReadonlyMode, PluginLogger}
import com.thoughtworks.pli.remotepair.core.client.{InWatchingList, PublishEvent}
import com.thoughtworks.pli.remotepair.idea.editor.GetSelectionEventInfo
import com.thoughtworks.pli.remotepair.idea.file.GetRelativePath

class HandleSelectionEvent(publishEvent: PublishEvent, logger: PluginLogger, inWatchingList: InWatchingList, getRelativePath: GetRelativePath, getSelectionEventInfo: GetSelectionEventInfo, isReadonlyMode: IsReadonlyMode) {
  def apply(event: IdeaSelectionChangeEvent): Unit = if (inWatchingList(event.file) && !isReadonlyMode()) {
    for {
      path <- getRelativePath(event.file)
      ee = SelectContentEvent(path, event.startOffset, event.length)
    } publishEvent(ee)
  }
}
