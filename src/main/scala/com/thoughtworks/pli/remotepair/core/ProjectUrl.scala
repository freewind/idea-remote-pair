package com.thoughtworks.pli.remotepair.core

case class ProjectUrl(host: String, port: Int, projectCode: String)

object ProjectUrl {
  def encode(projectUrl: ProjectUrl): String = {
    val ProjectUrl(host, port, projectCode) = projectUrl
    s"$host:$port:$projectCode"
  }
  def decode(url: String): ProjectUrl = url.split(":") match {
    case Array(host, port, projectCode) => ProjectUrl(host, port.toInt, projectCode)
  }
}
