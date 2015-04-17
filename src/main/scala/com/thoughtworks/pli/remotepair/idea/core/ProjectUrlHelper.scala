package com.thoughtworks.pli.remotepair.idea.core

class ProjectUrlHelper {
  def encode(projectUrl: ProjectUrl): String = {
    val ProjectUrl(host, port, projectCode) = projectUrl
    s"$host:$port:$projectCode"
  }
  def decode(url: String): ProjectUrl = url.split(":") match {
    case Array(host, port, projectCode) => ProjectUrl(host, port.toInt, projectCode)
  }
}
