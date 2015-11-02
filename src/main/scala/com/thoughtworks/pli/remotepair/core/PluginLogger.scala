package com.thoughtworks.pli.remotepair.core

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.remotepair.core.client.MyClient

class PluginLogger(logger: Logger, myClient: => MyClient) {

  def info(message: String): Unit = {
    logger.info(myName() + message)
  }

  def warn(message: String): Unit = {
    logger.warn(message)
  }

  def error(message: String): Unit = {
    logger.error(myName() + message)
  }

  def error(message: String, e: Throwable): Unit = {
    logger.error(myName() + message, e)
  }

  private def myName() = "[" + myClient.myClientName.getOrElse("unknown") + "] "

}
