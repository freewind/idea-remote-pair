package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.DefaultListModel
import com.thoughtworks.pli.intellij.remotepair.protocol.FileSummary
import com.thoughtworks.pli.intellij.remotepair.InvokeLater
import com.thoughtworks.pli.remotepair.idea.core.{InvokeLater, RichProject, PublishEvents, CurrentProjectHolder}

class ComparePairableFilesDialog(override val currentProject: RichProject)
  extends _ComparePairableFilesDialog with InvokeLater with PublishEvents with CurrentProjectHolder {

  def setPairFiles(clientName: String, pairFileSummaries: Seq[FileSummary]) = {
    val myFileSummaries = currentProject.getAllPairableFiles(currentProject.ignoredFiles).map(currentProject.getFileSummary)
    val pairFiles = pairFileSummaries.filterNot(myFileSummaries.contains).map(_.path)
    val myFiles = {
      val pairPaths = pairFileSummaries.map(_.path)
      val myPaths = myFileSummaries.map(_.path)
      myPaths.filterNot(pairPaths.contains)
    }
    pairClientNameLabel.setText(clientName)
    pairFilesList.setModel(createModel(pairFiles))
    myFilesList.setModel(createModel(myFiles))
  }

  private def createModel(paths: Seq[String]) = {
    val model = new DefaultListModel()
    paths.foreach(model.addElement)
    model
  }

}
