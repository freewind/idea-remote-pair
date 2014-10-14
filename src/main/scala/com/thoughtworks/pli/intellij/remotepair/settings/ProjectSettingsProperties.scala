package com.thoughtworks.pli.intellij.remotepair.settings

import com.thoughtworks.pli.intellij.remotepair.{DefaultValues, CurrentProjectHolder}
import DefaultValues._

trait ProjectSettingsProperties extends IdeaPluginServices with AppSettingsProperties {
  this: CurrentProjectHolder =>

  private val KeyProjectTargetServerHost = s"$PluginId.targetServerHost"
  private val KeyProjectTargetServerPort = s"$PluginId.targetServerPort"
  private val KeyProjectClientName = s"$PluginId.clientName"
  private val KeyTargetProject = s"$PluginId.targetProject"
  private val KeyIgnoredFiles = s"$PluginId.ignoredFiles"

  def projectProperties = new ProjectProperties

  class ProjectProperties {

    private val service = projectPropertiesService(currentProject)

    def targetServerHost_=(value: String) = service.setValue(KeyProjectTargetServerHost, value)

    def targetServerHost = Option(service.getValue(KeyProjectTargetServerHost)).getOrElse("")

    def targetServerPort_=(value: Int) = service.setValue(KeyProjectTargetServerPort, value.toString)

    def targetServerPort = service.getOrInitInt(KeyProjectTargetServerPort, DefaultValues.DefaultPort)

    def clientName_=(value: String) = service.setValue(KeyProjectClientName, value)

    def clientName = Option(service.getValue(KeyProjectClientName)).getOrElse(appProperties.defaultClientName)

    def targetProject_=(value: String) = service.setValue(KeyTargetProject, value)

    def targetProject = Option(service.getValue(KeyTargetProject)).getOrElse(currentProject.getName)

    def ignoredFiles_=(values: Seq[String]) = service.setValues(KeyIgnoredFiles, values.toArray)

    def ignoredFiles = Option(service.getValues(KeyIgnoredFiles)).fold(appProperties.defaultIgnoredFilesTemplate)(_.toSeq)

  }

}
