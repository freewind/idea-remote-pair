package com.thoughtworks.pli.intellij.remotepair.actions

import java.net.InetAddress

trait LocalHostInfo {

  def localIp() = InetAddress.getLocalHost.getHostAddress

  def localHostName() = InetAddress.getLocalHost.getHostName

}
