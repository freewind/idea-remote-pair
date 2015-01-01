package com.thoughtworks.pli.intellij.remotepair.actions.forms

import javax.swing.DefaultListModel

import com.thoughtworks.pli.intellij.remotepair.RichProject
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder

class ComparePairableFilesForm(override val currentProject: RichProject) extends _ComparePairableFilesForm with CurrentProjectHolder {

  def setPairFiles(pairClientName: String, pairFiles: Seq[String], myFiles: Seq[String]) = {
    getPairClientName.setText(pairClientName)
    getPairFiles.setModel(createModel(pairFiles))
    getMyFiles.setModel(createModel(myFiles))
  }

  private def createModel(paths: Seq[String]) = {
    val model = new DefaultListModel()
    paths.foreach(model.addElement)
    model
  }

}
