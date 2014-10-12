package com.thoughtworks.pli.intellij.remotepair.settings

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import com.intellij.ide.util.PropertiesComponent
import java.net.InetAddress

class SettingsConfigurable extends ApplicationComponent with Configurable {

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
      properties.port != settingsPanel.getPort ||
        properties.username != settingsPanel.getUsername ||
        properties.defaultIgnoredFiles != settingsPanel.getDefaultIgnoredFiles.toSeq)
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
    properties.port = settingsPanel.getPort
    properties.username = settingsPanel.getUsername
    properties.defaultIgnoredFiles = settingsPanel.getDefaultIgnoredFiles
  }

  override def reset(): Unit = {
    settingsPanel.setPort(properties.port)
    settingsPanel.setUsername(properties.username)
    settingsPanel.setDefaultIgnoredFiles(properties.defaultIgnoredFiles.toArray)
  }

  private def properties = new RemotePairProperties

}

class RemotePairProperties {
  val prefix = "com.thoughtworks.pli.intellij.remotepair"
  private val PORT = s"$prefix.port"
  private val USERNAME = s"$prefix.username"
  private val DEFAULT_IGNORED_FILES = s"$prefix.defaultIgnoredFiles"

  private val service = PropertiesComponent.getInstance()

  def port = service.getOrInitInt(PORT, 8888)

  def port_=(port: Int) = service.setValue(PORT, port.toString)

  def username = service.getValue(USERNAME, InetAddress.getLocalHost.getHostName)

  def username_=(value: String) = service.setValue(USERNAME, value)

  def defaultIgnoredFiles: Seq[String] = Option(service.getValues(DEFAULT_IGNORED_FILES)).map(_.toSeq).getOrElse(Nil)

  def defaultIgnoredFiles_=(values: Seq[String]) = service.setValues(DEFAULT_IGNORED_FILES, values.toArray)
}
