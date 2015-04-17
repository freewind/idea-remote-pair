package com.thoughtworks.pli.remotepair.idea.core

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class CopyToClipboard {
  def apply(content: String): Unit = {
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    clipboard.setContents(new StringSelection(content), null)
  }
}
