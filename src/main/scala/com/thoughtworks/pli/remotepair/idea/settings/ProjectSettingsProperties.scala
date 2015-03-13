package com.thoughtworks.pli.remotepair.idea.settings

import com.thoughtworks.pli.remotepair.idea.core.DefaultValues
import com.thoughtworks.pli.remotepair.idea.core.DefaultValues._
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

case class ProjectSettingsProperties(currentProject: RichProject, getCurrentProjectProperties: GetCurrentProjectProperties, appProperties: IdeaSettingsProperties) {

  private val KeyProjectTargetServerHost = s"$PluginId.targetServerHost"
  private val KeyProjectTargetServerPort = s"$PluginId.targetServerPort"
  private val KeyTargetProject = s"$PluginId.targetProject"
  private val KeyIgnoredFiles = s"$PluginId.ignoredFiles"


  private val service = getCurrentProjectProperties()

  def targetServerHost_=(value: String) = service.setValue(KeyProjectTargetServerHost, value)

  def targetServerHost = Option(service.getValue(KeyProjectTargetServerHost)).getOrElse("")

  def targetServerPort_=(value: Int) = service.setValue(KeyProjectTargetServerPort, value.toString)

  def targetServerPort = service.getOrInitInt(KeyProjectTargetServerPort, DefaultValues.DefaultPort)

  def targetProject_=(value: String) = service.setValue(KeyTargetProject, value)

  def targetProject = Option(service.getValue(KeyTargetProject)).getOrElse(currentProject.raw.getName)

  def ignoredFiles_=(values: Seq[String]) = service.setValues(KeyIgnoredFiles, values.toArray)

  def ignoredFiles = Option(service.getValues(KeyIgnoredFiles)).fold(appProperties.defaultIgnoredFilesTemplate)(_.toSeq)


}
