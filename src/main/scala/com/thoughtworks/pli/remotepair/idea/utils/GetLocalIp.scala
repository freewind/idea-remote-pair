package com.thoughtworks.pli.remotepair.idea.utils

import java.net.InetAddress

class GetLocalIp {
  def apply() = InetAddress.getLocalHost.getHostAddress
}
