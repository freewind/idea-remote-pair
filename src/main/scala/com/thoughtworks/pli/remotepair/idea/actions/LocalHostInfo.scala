package com.thoughtworks.pli.remotepair.idea.actions

import java.net.InetAddress

trait LocalHostInfo {

  def localIp() = InetAddress.getLocalHost.getHostAddress

  def localHostName() = InetAddress.getLocalHost.getHostName

}
