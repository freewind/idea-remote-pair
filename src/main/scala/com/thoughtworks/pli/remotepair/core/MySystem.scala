package com.thoughtworks.pli.remotepair.core

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.InetAddress

class MySystem {

  def now: Long = System.currentTimeMillis()

  def copyToClipboard(content: String): Unit = {
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    clipboard.setContents(new StringSelection(content), null)
  }

  def localIp: String = InetAddress.getLocalHost.getHostAddress

  def localHostName: String = InetAddress.getLocalHost.getHostName

}
