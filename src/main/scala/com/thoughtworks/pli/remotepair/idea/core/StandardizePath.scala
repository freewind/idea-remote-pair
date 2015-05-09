package com.thoughtworks.pli.remotepair.idea.core

import org.apache.commons.lang.StringUtils

class StandardizePath {
  def apply(path: String) = {
    StringUtils.stripEnd(path, "./")
  }
}
