package com.thoughtworks.pli.remotepair.in_memory_ide.models

import com.thoughtworks.pli.remotepair.core.models.{DataKey, MyEditor, MyFile, MyProject}

class MemoryProject(fs: MemoryFileSystem) extends MyProject {

  implicit def myfile2memory(file: MyFile): MemoryFile = file.asInstanceOf[MemoryFile]

  private var _openedFiles = Seq.empty[MemoryFile]
  private var _messageInDialog = Option.empty[String]
  var activeFile: Option[MemoryFile] = None
  def close(file: MemoryFile): Unit = _openedFiles = _openedFiles.filterNot(_ == file)

  private var userData = Map.empty[DataKey[_], Any]

  override def getUserData[T](key: DataKey[T]): Option[T] = Option(userData.get(key)).map(_.asInstanceOf[T])
  override def putUserData[T](key: DataKey[T], value: T): Unit = userData += (key -> value)
  override def showMessageDialog(message: String): Unit = _messageInDialog = Some(message)
  override def openFileInTab(file: MyFile): Unit = _openedFiles = _openedFiles :+ file.asInstanceOf[MemoryFile]
  override def findOrCreateDir(relativePath: String): MyFile = fs.findOrCreateDir(new Path(relativePath))
  override def findOrCreateFile(relativePath: String): MyFile = fs.findOrCreateFile(new Path(relativePath))
  override def showErrorDialog(title: String, message: String): Unit = ???
  override val openedFiles: Seq[MemoryFile] = _openedFiles
  override def notifyUserDataChanges(): Unit = ???
  override def getRelativePath(path: String): Option[String] = ??? //new Path(path)
  override def getTextEditorsOfPath(relativePath: String): Seq[MyEditor] = ???
  override def getFileByRelative(relativePath: String): Option[MyFile] = ???
  override def baseDir: MemoryFile = fs.rootFile
  override def isActive(file: MyFile): Boolean = activeFile.contains(file)
  override def close(file: MyFile): Unit = _openedFiles.filterNot(_ == file)
  override def isOpened(file: MyFile): Boolean = _openedFiles.contains(file)
  def openFile(path: String): Unit = fs.findFile(new Path(path)) match {
    case Some(file) => _openedFiles = _openedFiles :+ file
    case _ =>
  }

}
