package com.thoughtworks.pli.remotepair.core.models

trait MyProjectStorage {
  def serverHost: Option[String]
  def serverHost_=(value: String): Unit
  def serverPort: Option[Int]
  def serverPort_=(value: Int): Unit
  def clientName: Option[String]
  def clientName_=(value: String): Unit
  def projectName: Option[String]
  def projectName_=(value: String): Unit
  def projectUrl: Option[String]
  def projectUrl_=(value: String): Unit
}
