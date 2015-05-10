package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.remotepair.idea.project.GetMessageBus

class NotifyChanges(getMessageBus: GetMessageBus) {
  def apply(): Unit = {
    getMessageBus().foreach(ProjectStatusChanges.notify)
  }
}
