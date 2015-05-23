package com.thoughtworks.pli.remotepair.core

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.remotepair.core.client.ConnectedClient

class PluginLogger(logger: Logger, connectedClient: ConnectedClient) {

  def info(message: String): Unit = {
    logger.info(myName() + message)
  }

  def error(message: String): Unit = {
    logger.error(myName() + message)
  }

  def error(message: String, e: Throwable): Unit = {
    logger.error(myName() + message, e)
  }

  private def myName() = "[" + connectedClient.myClientName.getOrElse("unknown") + "] "

}
