package com.thoughtworks.pli.remotepair.idea.settings

import com.thoughtworks.pli.remotepair.idea.actions.LocalHostInfo
import com.thoughtworks.pli.remotepair.idea.core.DefaultValues

trait AppSettingsProperties extends LocalHostInfo with IdeaPluginServices {

  private val KeyServerBindingPort = s"${DefaultValues.PluginId}.serverBindingPort"
  private val KeyClientName = s"${DefaultValues.PluginId}.clientName"
  private val KeyDefaultIgnoredFiles = s"${DefaultValues.PluginId}.defaultIgnoredFiles"

  def appProperties = new AppProperties

  class AppProperties {
    private val service = appPropertiesService

    def serverBindingPort = service.getOrInitInt(KeyServerBindingPort, DefaultValues.DefaultPort)

    def serverBindingPort_=(port: Int) = service.setValue(KeyServerBindingPort, port.toString)

    def clientName = Option(service.getValue(KeyClientName)).getOrElse(localHostName())

    def clientName_=(value: String) = service.setValue(KeyClientName, value)

    def defaultIgnoredFilesTemplate: Seq[String] = Option(service.getValues(KeyDefaultIgnoredFiles)).map(_.toSeq).getOrElse(Nil)

    def defaultIgnoredFilesTemplate_=(values: Seq[String]) = service.setValues(KeyDefaultIgnoredFiles, values.toArray)
  }

}

