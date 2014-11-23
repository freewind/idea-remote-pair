package com.thoughtworks.pli.intellij.remotepair.actions.forms

import java.io.File
import java.awt.event.{ActionEvent, ActionListener}
import org.apache.commons.io.FileUtils
import scala.io.Source
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder

class IgnoreFilesForm extends _IgnoreFilesForm with GitIgnoreLoader {
  this: CurrentProjectHolder =>
  getMergeFromGitIgnoreButton.addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent) = {
      def readLines(f: File) = getLines(FileUtils.readFileToString(f, "UTF-8"))
      findGitIgnoreFile.map(readLines).map(fileLines => merge(fileLines, getFileList)).foreach { lines =>
        getFilesContext.setText(lines.mkString("\n"))
      }
    }
  })

  def getFileList = getLines(getFilesContext.getText)

  private def merge(lines1: List[String], lines2: List[String]) = (lines1 ::: lines2).toSet.toList.sorted

  private def getLines(text: String): List[String] = Source.fromString(text).getLines().toList.map(_.trim).filterNot(_.isEmpty)

}

trait GitIgnoreLoader {
  this: CurrentProjectHolder =>
  def findGitIgnoreFile: Option[File] = currentProject.getByRelative(".gitignore").map(_.getPath).map(new File(_))
}
