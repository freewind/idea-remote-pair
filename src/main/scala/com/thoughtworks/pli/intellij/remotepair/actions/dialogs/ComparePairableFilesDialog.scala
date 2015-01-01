package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.thoughtworks.pli.intellij.remotepair.actions.forms.ComparePairableFilesForm
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.protocol.FileSummary
import com.thoughtworks.pli.intellij.remotepair.{InvokeLater, PublishEvents, RichProject}

class ComparePairableFilesDialog(override val currentProject: RichProject) extends DialogWrapper(currentProject.raw) with InvokeLater with PublishEvents with CurrentProjectHolder {

  init()

  private object EarlyInit {
    val form = new ComparePairableFilesForm(currentProject)
  }

  private def form = EarlyInit.form

  override def createCenterPanel() = {
    form.getMainPanel
  }

  def setPairFiles(clientName: String, pairFileSummaries: Seq[FileSummary]) = {
    val myFileSummaries = currentProject.getAllPairableFiles(currentProject.ignoredFiles).map(currentProject.getFileSummary)
    val pairFiles = pairFileSummaries.filterNot(myFileSummaries.contains).map(_.path)
    val myFiles = {
      val pairPaths = pairFileSummaries.map(_.path)
      val myPaths = myFileSummaries.map(_.path)
      myPaths.filterNot(pairPaths.contains)
    }
    form.setPairFiles(clientName, pairFiles, myFiles)
  }

}
