package com.thoughtworks.pli.remotepair.idea.settings

import javax.swing.JComponent

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.options.Configurable
import com.thoughtworks.pli.remotepair.idea.UtilsModule

class SettingsConfigurable extends ApplicationComponent with Configurable with UtilsModule {

  private var settingsPanel: SettingsPanel = _

  override def initComponent(): Unit = {
    ideaLogger.info("init component")
  }

  override def disposeComponent(): Unit = {
    ideaLogger.info("dispose component")
    if (settingsPanel != null) {
      this.settingsPanel = null
    }
  }

  override def getComponentName: String = {
    ideaLogger.info("getComponentName")
    "RemotePairSettingsConfigurable"
  }

  override def getDisplayName: String = {
    ideaLogger.info("getDisplayName")
    "Remote Pair"
  }

  override def getHelpTopic: String = {
    ideaLogger.info("getHelpTopic")
    "RemotePair help topic"
  }

  override def isModified: Boolean = {
    false
  }

  override def createComponent(): JComponent = {
    ideaLogger.info("createComponent")
    if (settingsPanel == null) {
      settingsPanel = new SettingsPanel
    }
    settingsPanel.getPanel
  }

  override def disposeUIResources(): Unit = {}

  override def apply(): Unit = {
  }

  override def reset(): Unit = {
  }

}



