package com.thoughtworks.pli.remotepair.core

import java.awt.{Color, Graphics}
import javax.swing.JComponent

class PairCaretComponent extends JComponent {
  var lineHeight: Int = 0

  override def paint(g: Graphics): Unit = {
    g.setColor(Color.RED)
    g.fillRect(0, 0, 2, lineHeight)
    super.paint(g)
  }
}
