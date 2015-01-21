package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.JPanel

import com.thoughtworks.pli.remotepair.idea.core.{CurrentProjectHolder, RichProject}

class TestDialog(override val currentProject: RichProject) extends _TestDialog with JDialogSupport with CurrentProjectHolder {

  override def getContentPanel: JPanel = contentPanel

}
