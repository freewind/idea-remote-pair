package com.thoughtworks.pli.remotepair.idea.settings

import com.thoughtworks.pli.remotepair.idea.core.DefaultValues
import com.thoughtworks.pli.remotepair.idea.utils.GetLocalHostName

class ServerPortInGlobalStorage(getIdeaProperties: GetIdeaProperties) {
  private val KeyServerBindingPort = s"${DefaultValues.PluginId}.serverBindingPort"
  def load(): Int = getIdeaProperties().getOrInitInt(KeyServerBindingPort, DefaultValues.DefaultPort)
  def save(port: Int): Unit = getIdeaProperties().setValue(KeyServerBindingPort, port.toString)
}

class ClientNameInGlobalStorage(getIdeaProperties: GetIdeaProperties, getLocalHostName: GetLocalHostName) {
  private val KeyClientName = s"${DefaultValues.PluginId}.clientName"
  def load(): String = Option(getIdeaProperties().getValue(KeyClientName)).getOrElse(getLocalHostName())
  def save(value: String): Unit = getIdeaProperties().setValue(KeyClientName, value)
}

