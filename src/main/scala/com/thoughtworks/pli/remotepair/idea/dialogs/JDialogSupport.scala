package com.thoughtworks.pli.remotepair.idea.dialogs

import java.awt.event._
import javax.swing._

trait JDialogSupport {
  this: JDialog =>
  def contentPane: JPanel
  def buttonOK: JButton
  def buttonCancel: JButton

  def onOK(): Unit
  def onCancel(): Unit

  setContentPane(contentPane)
  setModal(true)
  getRootPane.setDefaultButton(buttonOK)
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  buttonOK.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      onOK()
      dispose()
    }
  })

  buttonCancel.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      onCancel()
      dispose()
    }
  })


  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent) {
      onCancel()
      dispose()
    }
  })

  contentPane.registerKeyboardAction(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      onCancel()
      dispose()
    }
  }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

}
