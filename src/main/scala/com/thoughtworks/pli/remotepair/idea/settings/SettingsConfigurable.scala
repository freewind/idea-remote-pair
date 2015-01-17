package com.thoughtworks.pli.remotepair.idea.settings

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import com.thoughtworks.pli.remotepair.idea.actions.LocalHostInfo

class SettingsConfigurable extends ApplicationComponent with Configurable
with AppSettingsProperties with IdeaPluginServices with LocalHostInfo {

  private var settingsPanel: SettingsPanel = _

  private final val log: Logger = Logger.getInstance(classOf[SettingsConfigurable])

  override def initComponent(): Unit = {
    log.info("### init component")
  }

  override def disposeComponent(): Unit = {
    log.info("### dispose component")
    if (settingsPanel != null) {
      this.settingsPanel = null
    }
  }

  override def getComponentName: String = {
    log.info("### getComponentName")
    "RemotePairSettingsConfigurable"
  }

  override def getDisplayName: String = {
    log.info("### getDisplayName")
    "Remote Pair"
  }

  override def getHelpTopic: String = {
    log.info("### getHelpTopic")
    "RemotePair help topic"
  }

  override def isModified: Boolean = {
    settingsPanel != null && (
      properties.serverBindingPort != settingsPanel.getPort ||
        properties.clientName != settingsPanel.getUsername ||
        properties.defaultIgnoredFilesTemplate != settingsPanel.getDefaultIgnoredFiles.toSeq)
  }

  override def createComponent(): JComponent = {
    log.info("### createComponent")
    if (settingsPanel == null) {
      settingsPanel = new SettingsPanel
    }
    settingsPanel.getPanel
  }

  override def disposeUIResources(): Unit = {}

  override def apply(): Unit = {
    properties.serverBindingPort = settingsPanel.getPort
    properties.clientName = settingsPanel.getUsername
    properties.defaultIgnoredFilesTemplate = settingsPanel.getDefaultIgnoredFiles
  }

  override def reset(): Unit = {
    settingsPanel.setPort(properties.serverBindingPort)
    settingsPanel.setUsername(properties.clientName)
    settingsPanel.setDefaultIgnoredFiles(properties.defaultIgnoredFilesTemplate.toArray)
  }

  private def properties = appProperties

}



