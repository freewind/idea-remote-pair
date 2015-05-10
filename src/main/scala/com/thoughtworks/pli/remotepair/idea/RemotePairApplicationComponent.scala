package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger

class RemotePairApplicationComponent extends ApplicationComponent {

  private final val log: Logger = Logger.getInstance(classOf[RemotePairApplicationComponent])

  override def initComponent(): Unit = {
    log.info("init component")
  }

  override def disposeComponent(): Unit = {
    log.info("dispose component")
  }

  override def getComponentName: String = {
    log.info("getComponentName")
    "IdeaRemotePair"
  }
}
