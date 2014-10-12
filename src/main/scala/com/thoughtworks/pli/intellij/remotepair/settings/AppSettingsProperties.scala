package com.thoughtworks.pli.intellij.remotepair.settings

import com.intellij.ide.util.PropertiesComponent
import com.thoughtworks.pli.intellij.remotepair.actions.LocalHostInfo

object AppSettingsProperties {
  val DefaultPort = 8888
}

trait AppSettingsProperties {
  this: ObjectsHolder with LocalHostInfo =>

  private val Prefix = "com.thoughtworks.pli.intellij.remotepair"
  private val KeyPort = s"$Prefix.serverBindingPort"
  private val KeyUsername = s"$Prefix.clientName"
  private val KeyDefaultIgnoredFiles = s"$Prefix.defaultIgnoredFiles"

  def appProperties = new {
    private val service = appPropertiesService

    def serverBindingPort = service.getOrInitInt(KeyPort, AppSettingsProperties.DefaultPort)

    def serverBindingPort_=(port: Int) = service.setValue(KeyPort, port.toString)

    def clientName = service.getValue(KeyUsername, localHostName())

    def clientName_=(value: String) = service.setValue(KeyUsername, value)

    def defaultIgnoredFilesTemplate: Seq[String] = Option(service.getValues(KeyDefaultIgnoredFiles)).map(_.toSeq).getOrElse(Nil)

    def defaultIgnoredFilesTemplate_=(values: Seq[String]) = service.setValues(KeyDefaultIgnoredFiles, values.toArray)
  }
}

trait ObjectsHolder {
  def appPropertiesService = ObjectsHolder.propertiesService
}

object ObjectsHolder {
  private val propertiesService = PropertiesComponent.getInstance()
}