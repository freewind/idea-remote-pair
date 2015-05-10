package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.remotepair.core.ConnectionHolder

class CloseConnection(connectionHolder: ConnectionHolder) {
  def apply(): Unit = connectionHolder.get.foreach(_.close())
}
