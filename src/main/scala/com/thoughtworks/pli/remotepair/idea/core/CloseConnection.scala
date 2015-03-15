package com.thoughtworks.pli.remotepair.idea.core

class CloseConnection(connectionHolder: ConnectionHolder) {
  def apply(): Unit = connectionHolder.get.foreach(_.close())
}
