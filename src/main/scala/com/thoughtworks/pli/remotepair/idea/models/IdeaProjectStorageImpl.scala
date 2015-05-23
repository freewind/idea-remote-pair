package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.ide.util.PropertiesComponent
import com.thoughtworks.pli.remotepair.core.models.MyProjectStorage
import com.thoughtworks.pli.remotepair.idea.DefaultValues._

class IdeaProjectStorageImpl(currentProject: IdeaProjectImpl) extends MyProjectStorage {
  private object Keys {
    private def key(name: String) = s"$PluginId.project.$name"
    val serverHost = key("serverHost")
    val serverPort = key("serverPort")
    val clientName = key("clientName")
    val projectName = key("projectName")
    val projectUrl = key("projectUrl")
  }

  override def serverHost: Option[String] = Option(propertiesComponent.getValue(Keys.serverHost))
  override def serverHost_=(value: String): Unit = propertiesComponent.setValue(Keys.serverHost, value)
  override def serverPort: Option[Int] = Option(propertiesComponent.getValue(Keys.serverPort)).map(_.toInt)
  override def serverPort_=(value: Int): Unit = propertiesComponent.setValue(Keys.serverPort, value.toString)
  override def clientName: Option[String] = Option(propertiesComponent.getValue(Keys.clientName))
  override def clientName_=(value: String): Unit = propertiesComponent.setValue(Keys.clientName, value)
  override def projectName: Option[String] = Option(propertiesComponent.getValue(Keys.projectName))
  override def projectName_=(value: String): Unit = propertiesComponent.setValue(Keys.projectName, value)
  override def projectUrl: Option[String] = Option(propertiesComponent.getValue(Keys.projectUrl))
  override def projectUrl_=(value: String): Unit = propertiesComponent.setValue(Keys.projectUrl, value)

  private def propertiesComponent = PropertiesComponent.getInstance(currentProject.rawProject)
}
