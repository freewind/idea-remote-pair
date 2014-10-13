package com.thoughtworks.pli.intellij.remotepair.settings

import com.intellij.ide.util.PropertiesComponent
import com.thoughtworks.pli.intellij.remotepair.actions.LocalHostInfo
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.DefaultValues

trait AppSettingsProperties {
  this: IdeaPluginServices with LocalHostInfo =>

  private val KeyServerBindingPort = s"${DefaultValues.PluginId}.serverBindingPort"
  private val KeyDefaultClientName = s"${DefaultValues.PluginId}.defaultClientName"
  private val KeyDefaultIgnoredFiles = s"${DefaultValues.PluginId}.defaultIgnoredFiles"

  def appProperties = new AppProperties

  class AppProperties {
    private val service = appPropertiesService

    def serverBindingPort = service.getOrInitInt(KeyServerBindingPort, DefaultValues.DefaultPort)

    def serverBindingPort_=(port: Int) = service.setValue(KeyServerBindingPort, port.toString)

    def defaultClientName = Option(service.getValue(KeyDefaultClientName)).getOrElse(localHostName())

    def defaultClientName_=(value: String) = service.setValue(KeyDefaultClientName, value)

    def defaultIgnoredFilesTemplate: Seq[String] = Option(service.getValues(KeyDefaultIgnoredFiles)).map(_.toSeq).getOrElse(Nil)

    def defaultIgnoredFilesTemplate_=(values: Seq[String]) = service.setValues(KeyDefaultIgnoredFiles, values.toArray)
  }

}

trait IdeaPluginServices {
  def appPropertiesService: PropertiesComponent = PropertiesComponent.getInstance()

  def projectPropertiesService(project: Project): PropertiesComponent = PropertiesComponent.getInstance(project)
}