//package com.thoughtworks.pli.remotepair.idea.core
//
//import com.intellij.openapi.vfs.VirtualFile
//import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory._
//
//import scala.io.Source
//
//class GuessIgnoreFilesFrom(currentProject: RichProject) {
//  def apply(root: VirtualFile): Seq[String] = {
//    def getIdeaDotFiles: Seq[String] = {
//      val files = currentProject.getBaseDir.getChildren
//        .filter(file => file.getName.startsWith(".") || file.getName.endsWith(".iml"))
//        .map(currentProject.getRelativePath).flatten
//      files.toSeq filter (_ != "/.gitignore")
//    }
//    def readLines(f: VirtualFile) = {
//      val content = currentProject.getFileContent(f)
//      Source.fromString(content.text).getLines().toList.map(_.trim).filterNot(_.isEmpty).filterNot(_.startsWith("#"))
//    }
//    val filesFromGitIgnore = findGitIgnoreFile.map(readLines).getOrElse(Nil).flatMap(toRealPath)
//    filesFromGitIgnore ++: getIdeaDotFiles
//  }
//
//  private def findGitIgnoreFile: Option[VirtualFile] = currentProject.getFileByRelative("/.gitignore")
//  private def toRealPath(patten: String): Option[String] = {
//    def fixCurrentPrefix(p: String) = p.replaceAll("^[.]/", "/")
//    def fixGlobalPrefix(p: String) = if (p.startsWith("/")) p else "/" + p
//    def removeEndingStar(p: String) = p.replaceAll("[*]+$", "")
//    def removeEndingSlash(p: String) = p.replaceAll("[/]$", "")
//    def isNotValid(path: String) = path.contains("*") || path.endsWith("\\") || currentProject.getFileByRelative(path).isEmpty
//
//    val fixedPath = Option(patten).map(fixCurrentPrefix).map(fixGlobalPrefix).map(removeEndingStar).map(removeEndingSlash)
//    fixedPath.filterNot(isNotValid)
//  }
//
//
//}
