package com.thoughtworks.pli.remotepair.idea.settings

import com.thoughtworks.pli.remotepair.idea.core.DefaultValues._

trait ValueInProjectStorage[T] {
  def getCurrentProjectProperties: GetCurrentProjectProperties
  def valueToString(value: T): String = value.toString
  def stringToValueType(s: String): T = s.asInstanceOf[T]

  private val key = s"$PluginId.project.${this.getClass.getSimpleName}"
  def save(value: T) = getCurrentProjectProperties().setValue(key, valueToString(value))
  def load(): Option[T] = Option(getCurrentProjectProperties().getValue(key)).map(stringToValueType)
}

class ServerHostInProjectStorage(val getCurrentProjectProperties: GetCurrentProjectProperties) extends ValueInProjectStorage[String]

class ServerPortInProjectStorage(val getCurrentProjectProperties: GetCurrentProjectProperties) extends ValueInProjectStorage[Int] {
  override def stringToValueType(s: String): Int = s.toInt
}

class ClientNameInCreationInProjectStorage(val getCurrentProjectProperties: GetCurrentProjectProperties) extends ValueInProjectStorage[String]

class ClientNameInJoinInProjectStorage(val getCurrentProjectProperties: GetCurrentProjectProperties) extends ValueInProjectStorage[String]

class ProjectNameInProjectStorage(val getCurrentProjectProperties: GetCurrentProjectProperties) extends ValueInProjectStorage[String]

class ProjectUrlInProjectStorage(val getCurrentProjectProperties: GetCurrentProjectProperties) extends ValueInProjectStorage[String]
