package com.thoughtworks.pli.intellij.remotepair.settings

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import com.thoughtworks.pli.intellij.remotepair.actions.LocalHostInfo

class SettingsConfigurable extends ApplicationComponent with Configurable
with AppSettingsProperties with IdeaPluginServices with LocalHostInfo {

  var settingsPanel: SettingsPanel = _

  private final val log: Logger = Logger.getInstance(classOf[SettingsConfigurable])

  override def initComponent(): Unit = {
    log.info("### init component")
    println("### init component")
  }

  override def disposeComponent(): Unit = {
    log.info("### dispose component")
    println("### dispose component")
    if (settingsPanel != null) {
      this.settingsPanel = null
    }
  }

  override def getComponentName: String = {
    log.info("### getComponentName")
    println("### getComponentName")
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
        properties.defaultClientName != settingsPanel.getUsername ||
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
    properties.defaultClientName = settingsPanel.getUsername
    properties.defaultIgnoredFilesTemplate = settingsPanel.getDefaultIgnoredFiles
  }

  override def reset(): Unit = {
    settingsPanel.setPort(properties.serverBindingPort)
    settingsPanel.setUsername(properties.defaultClientName)
    settingsPanel.setDefaultIgnoredFiles(properties.defaultIgnoredFilesTemplate.toArray)
  }

  private def properties = appProperties

}



