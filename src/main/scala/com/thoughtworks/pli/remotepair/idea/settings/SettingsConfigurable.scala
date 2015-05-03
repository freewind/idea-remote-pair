package com.thoughtworks.pli.remotepair.idea.settings

import javax.swing.JComponent

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.options.Configurable
import com.thoughtworks.pli.remotepair.idea.UtilsModule

class SettingsConfigurable extends ApplicationComponent with Configurable with UtilsModule {

  private var settingsPanel: SettingsPanel = _

  override def initComponent(): Unit = {
    logger.info("### init component")
  }

  override def disposeComponent(): Unit = {
    logger.info("### dispose component")
    if (settingsPanel != null) {
      this.settingsPanel = null
    }
  }

  override def getComponentName: String = {
    logger.info("### getComponentName")
    "RemotePairSettingsConfigurable"
  }

  override def getDisplayName: String = {
    logger.info("### getDisplayName")
    "Remote Pair"
  }

  override def getHelpTopic: String = {
    logger.info("### getHelpTopic")
    "RemotePair help topic"
  }

  override def isModified: Boolean = {
    settingsPanel != null && (serverPortInGlobalStorage.load() != settingsPanel.getPort || clientNameInGlobalStorage.load() != settingsPanel.getUsername)
  }

  override def createComponent(): JComponent = {
    logger.info("### createComponent")
    if (settingsPanel == null) {
      settingsPanel = new SettingsPanel
    }
    settingsPanel.getPanel
  }

  override def disposeUIResources(): Unit = {}

  override def apply(): Unit = {
    serverPortInGlobalStorage.save(settingsPanel.getPort)
    clientNameInGlobalStorage.save(settingsPanel.getUsername)
  }

  override def reset(): Unit = {
    settingsPanel.setPort(serverPortInGlobalStorage.load())
    settingsPanel.setUsername(clientNameInGlobalStorage.load())
  }

}



