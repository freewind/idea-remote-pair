package com.thoughtworks.pli.intellij.remotepair.utils

import java.security.MessageDigest

trait Md5Support {
  def md5(s: String): String = {
    MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02X".format(_)).mkString
  }
}
