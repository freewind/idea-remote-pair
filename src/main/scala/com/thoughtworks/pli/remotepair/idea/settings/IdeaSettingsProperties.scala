package com.thoughtworks.pli.remotepair.idea.settings

import com.thoughtworks.pli.remotepair.idea.core.DefaultValues
import com.thoughtworks.pli.remotepair.idea.utils.GetLocalHostName

class ServerPortInStorage(getIdeaProperties: GetIdeaProperties) {
  private val KeyServerBindingPort = s"${DefaultValues.PluginId}.serverBindingPort"
  def load() = getIdeaProperties().getOrInitInt(KeyServerBindingPort, DefaultValues.DefaultPort)
  def save(port: Int) = getIdeaProperties().setValue(KeyServerBindingPort, port.toString)
}

class DefaultIgnoredFilesInStorage(getIdeaProperties: GetIdeaProperties) {
  private val KeyDefaultIgnoredFiles = s"${DefaultValues.PluginId}.defaultIgnoredFiles"
  def load(): Seq[String] = Option(getIdeaProperties().getValues(KeyDefaultIgnoredFiles)).map(_.toSeq).getOrElse(Nil)
  def save(values: Seq[String]) = getIdeaProperties().setValues(KeyDefaultIgnoredFiles, values.toArray)
}

class ClientNameInStorage(getIdeaProperties: GetIdeaProperties, getLocalHostName: GetLocalHostName) {
  private val KeyClientName = s"${DefaultValues.PluginId}.clientName"
  def load() = Option(getIdeaProperties().getValue(KeyClientName)).getOrElse(getLocalHostName())
  def save(value: String) = getIdeaProperties().setValue(KeyClientName, value)
}

