package com.thoughtworks.pli.remotepair.idea.settings

import com.thoughtworks.pli.remotepair.idea.core.{DefaultValues, CurrentProjectHolder}
import DefaultValues._
import com.thoughtworks.pli.remotepair.idea.core.{DefaultValues, CurrentProjectHolder}

trait ProjectSettingsProperties extends IdeaPluginServices with AppSettingsProperties {
  this: CurrentProjectHolder =>

  private val KeyProjectTargetServerHost = s"$PluginId.targetServerHost"
  private val KeyProjectTargetServerPort = s"$PluginId.targetServerPort"
  private val KeyTargetProject = s"$PluginId.targetProject"
  private val KeyIgnoredFiles = s"$PluginId.ignoredFiles"

  def projectProperties = new ProjectProperties

  class ProjectProperties {

    private val service = projectPropertiesService(currentProject.raw)

    def targetServerHost_=(value: String) = service.setValue(KeyProjectTargetServerHost, value)

    def targetServerHost = Option(service.getValue(KeyProjectTargetServerHost)).getOrElse("")

    def targetServerPort_=(value: Int) = service.setValue(KeyProjectTargetServerPort, value.toString)

    def targetServerPort = service.getOrInitInt(KeyProjectTargetServerPort, DefaultValues.DefaultPort)

    def targetProject_=(value: String) = service.setValue(KeyTargetProject, value)

    def targetProject = Option(service.getValue(KeyTargetProject)).getOrElse(currentProject.raw.getName)

    def ignoredFiles_=(values: Seq[String]) = service.setValues(KeyIgnoredFiles, values.toArray)

    def ignoredFiles = Option(service.getValues(KeyIgnoredFiles)).fold(appProperties.defaultIgnoredFilesTemplate)(_.toSeq)

  }

}
