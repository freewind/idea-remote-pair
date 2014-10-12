package com.thoughtworks.pli.intellij.remotepair.settings

import com.thoughtworks.pli.intellij.remotepair.{DefaultValues, CurrentProjectHolder}
import DefaultValues._

trait ProjectSettingsProperties {
  this: CurrentProjectHolder with IdeaPluginServices with AppSettingsProperties =>

  private val KeyProjectTargetServerHost = s"$PluginId.targetServerHost"
  private val KeyProjectTargetServerPort = s"$PluginId.targetServerPort"
  private val KeyProjectClientName = s"$PluginId.clientName"

  def projectProperties = new ProjectProperties

  class ProjectProperties {

    private val service = projectPropertiesService(currentProject)

    def targetServerHost_=(value: String) = service.setValue(KeyProjectTargetServerHost, value)

    def targetServerHost = Option(service.getValue(KeyProjectTargetServerHost)).getOrElse("")

    def targetServerPort_=(value: Int) = service.setValue(KeyProjectTargetServerPort, value.toString)

    def targetServerPort = service.getOrInitInt(KeyProjectTargetServerPort, DefaultValues.DefaultPort)

    def clientName_=(value: String) = service.setValue(KeyProjectClientName, value)

    def clientName = Option(service.getValue(KeyProjectClientName)).getOrElse(appProperties.clientName)
  }

}
