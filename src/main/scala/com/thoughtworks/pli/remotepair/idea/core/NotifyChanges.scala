package com.thoughtworks.pli.remotepair.idea.core

class NotifyChanges(getMessageBus: GetMessageBus) {
  def apply(): Unit = {
    getMessageBus().foreach(ProjectStatusChanges.notify)
  }
}
