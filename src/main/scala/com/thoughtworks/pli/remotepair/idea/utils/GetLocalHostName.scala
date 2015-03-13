package com.thoughtworks.pli.remotepair.idea.utils

import java.net.InetAddress

class GetLocalHostName {
  def apply() = InetAddress.getLocalHost.getHostName
}
