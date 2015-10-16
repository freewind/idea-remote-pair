package com.thoughtworks.pli.remotepair.in_memory_ide.models

import com.thoughtworks.pli.remotepair.core.models.{DataKey, MyEditor, MyFile, MyProject}

class MemoryProject(fs: MemoryFileSystem) extends MyProject {

  implicit def myfile2memory(file: MyFile): MemoryFile = file.asInstanceOf[MemoryFile]

  private var _openedFiles = Seq.empty[MemoryFile]
  private var _messageInDialog = Option.empty[String]
  var _editors = List.empty[MemoryEditor]

  var activeFile: Option[MemoryFile] = None
  def close(file: MemoryFile): Unit = _openedFiles = _openedFiles.filterNot(_ == file)

  private var userData = Map.empty[DataKey[_], Any]

  override def getUserData[T](key: DataKey[T]): Option[T] = Option(userData.get(key)).map(_.asInstanceOf[T])
  override def putUserData[T](key: DataKey[T], value: T, postAction: Option[() => Unit] = None): Unit = userData += (key -> value)
  override def getOrInitUserData[T](key: DataKey[T], initValue: T): T = ???
  override def showMessageDialog(message: String): Unit = _messageInDialog = Some(message)
  override def openFileInTab(file: MyFile): Unit = {
    _openedFiles = _openedFiles :+ file.asInstanceOf[MemoryFile]
    if (file.relativePath.exists(getTextEditorsOfPath(_).isEmpty)) {
      val content = file.content.text
      _editors = new MemoryEditor(new MemoryDocument(content), file.relativePath.get) :: _editors
    }
  }
  override def findOrCreateDir(relativePath: String): MyFile = fs.findOrCreateDir(new InMemoryFilePath(relativePath))
  override def findOrCreateFile(relativePath: String): MyFile = fs.findOrCreateFile(new InMemoryFilePath(relativePath))
  override def showErrorDialog(title: String, message: String): Unit = ???
  override val openedFiles: Seq[MemoryFile] = _openedFiles
  override def notifyUserDataChanges(): Unit = ???
  override def getRelativePath(absolutePath: String): Option[String] = new MemoryFile(new InMemoryFilePath(absolutePath), fs).relativePath
  override def getTextEditorsOfPath(relativePath: String): Seq[MyEditor] = {
    _editors.filter(_.filePath == relativePath)
  }
  override def getFileByRelative(relativePath: String): Option[MyFile] = Option(new MemoryFile(baseDir._path.child(relativePath), fs)).filter(_.exists)
  override def baseDir: MemoryFile = fs.rootDir
  override def isActive(file: MyFile): Boolean = activeFile.contains(file)
  override def close(file: MyFile): Unit = _openedFiles.filterNot(_ == file)
  override def isOpened(file: MyFile): Boolean = _openedFiles.contains(file)
  def openFile(path: String): Unit = fs.findFile(new InMemoryFilePath(path)) match {
    case Some(file) => openFileInTab(file)
    case _ =>
  }
}
