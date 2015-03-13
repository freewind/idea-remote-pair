package com.thoughtworks.pli.remotepair.idea.settings

import javax.swing.JComponent

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import com.softwaremill.macwire.Macwire
import com.thoughtworks.pli.remotepair.idea.UtilsModule
import com.thoughtworks.pli.remotepair.idea.settings.IdeaSettingsProperties
import com.thoughtworks.pli.remotepair.idea.utils.GetLocalHostName

class SettingsConfigurable extends ApplicationComponent with Configurable with UtilsModule {

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
      ideaSettingsProperties.serverBindingPort != settingsPanel.getPort ||
        ideaSettingsProperties.clientName != settingsPanel.getUsername ||
        ideaSettingsProperties.defaultIgnoredFilesTemplate != settingsPanel.getDefaultIgnoredFiles.toSeq)
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
    ideaSettingsProperties.serverBindingPort = settingsPanel.getPort
    ideaSettingsProperties.clientName = settingsPanel.getUsername
    ideaSettingsProperties.defaultIgnoredFilesTemplate = settingsPanel.getDefaultIgnoredFiles
  }

  override def reset(): Unit = {
    settingsPanel.setPort(ideaSettingsProperties.serverBindingPort)
    settingsPanel.setUsername(ideaSettingsProperties.clientName)
    settingsPanel.setDefaultIgnoredFiles(ideaSettingsProperties.defaultIgnoredFilesTemplate.toArray)
  }

}



