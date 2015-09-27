package com.thoughtworks.pli.remotepair.in_memory_ide.models

import com.thoughtworks.pli.remotepair.core.models.MyProjectStorage

class MemoryProjectStorage extends MyProjectStorage {
  private var _projectName: Option[String] = None
  private var _clientName: Option[String] = None
  private var _serverPort: Option[Int] = None
  private var _projectUrl: Option[String] = None
  private var _serverHost: Option[String] = None

  override def serverHost: Option[String] = _serverHost
  override def serverPort: Option[Int] = _serverPort
  override def clientName: Option[String] = _clientName
  override def projectName_=(value: String): Unit = _projectName = Some(value)
  override def clientName_=(value: String): Unit = _clientName = Some(value)
  override def serverPort_=(value: Int): Unit = _serverPort = Some(value)
  override def serverHost_=(value: String): Unit = _serverHost = Some(value)
  override def projectUrl: Option[String] = _projectUrl
  override def projectUrl_=(value: String): Unit = _projectUrl = Some(value)
  override def projectName: Option[String] = _projectName
}
