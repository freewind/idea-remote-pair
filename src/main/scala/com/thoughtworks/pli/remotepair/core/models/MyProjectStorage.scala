package com.thoughtworks.pli.remotepair.core.models

import com.intellij.ide.util.PropertiesComponent
import com.thoughtworks.pli.remotepair.core.DefaultValues

class MyProjectStorage(currentProject: MyProject) {

  private object Keys {
    private def key(name: String) = s"${DefaultValues.PluginId}.project.$name"
    val serverHost = key("serverHost")
    val serverPort = key("serverPort")
    val clientName = key("clientName")
    val projectName = key("projectName")
    val projectUrl = key("projectUrl")
  }

  def serverHost: Option[String] = Option(propertiesComponent.getValue(Keys.serverHost))
  def serverHost_=(value: String): Unit = propertiesComponent.setValue(Keys.serverHost, value)
  def serverPort: Option[Int] = Option(propertiesComponent.getValue(Keys.serverPort)).map(_.toInt)
  def serverPort_=(value: Int): Unit = propertiesComponent.setValue(Keys.serverPort, value.toString)
  def clientName: Option[String] = Option(propertiesComponent.getValue(Keys.clientName))
  def clientName_=(value: String): Unit = propertiesComponent.setValue(Keys.clientName, value)
  def projectName: Option[String] = Option(propertiesComponent.getValue(Keys.projectName))
  def projectName_=(value: String): Unit = propertiesComponent.setValue(Keys.projectName, value)
  def projectUrl: Option[String] = Option(propertiesComponent.getValue(Keys.projectUrl))
  def projectUrl_=(value: String): Unit = propertiesComponent.setValue(Keys.projectUrl, value)

  private def propertiesComponent = PropertiesComponent.getInstance(currentProject.rawProject)
}
