package com.thoughtworks.pli.remotepair.in_memory_ide.models

import com.thoughtworks.pli.remotepair.core.models.MyProjectStorage

class MemoryProjectStorage extends MyProjectStorage {
  override def serverHost: Option[String] = ???
  override def serverPort: Option[Int] = ???
  override def clientName: Option[String] = ???
  override def projectName_=(value: String): Unit = ???
  override def clientName_=(value: String): Unit = ???
  override def serverPort_=(value: Int): Unit = ???
  override def serverHost_=(value: String): Unit = ???
  override def projectUrl: Option[String] = ???
  override def projectUrl_=(value: String): Unit = ???
  override def projectName: Option[String] = ???
}
