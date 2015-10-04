package com.thoughtworks.pli.remotepair.in_memory_ide.models

import java.io.{FileNotFoundException, File}

import com.thoughtworks.pli.intellij.remotepair.protocol.{FileSummary, Content}
import com.thoughtworks.pli.intellij.remotepair.utils.Md5
import com.thoughtworks.pli.remotepair.core.models.MyFile
import org.apache.commons.io.FileUtils

case class InMemoryFilePath(path: String) {
  require(path.startsWith("/"), "path should starts with /: " + path)

  def relativeTo(base: InMemoryFilePath): Option[String] = base.items.foldLeft(Option(this)) {
    case (Some(result), item) if result.items.headOption.contains(item) => Some(new InMemoryFilePath(makePath(result.items.tail)))
    case _ => None
  }.map(_.toString)

  def isRoot: Boolean = items.isEmpty
  def filename: String = items.lastOption.getOrElse("/")
  def items: Seq[String] = {
    val ss = path.split("/")
    if (ss.isEmpty) ss else ss.tail
  }
  def rename(newName: String) = {
    if (isRoot) throw new UnsupportedOperationException("can't rename root")
    else new InMemoryFilePath(makePath(items.init :+ newName))
  }
  def child(fileName: String) = new InMemoryFilePath(makePath(items :+ fileName))
  def parent = if (isRoot) this else new InMemoryFilePath(makePath(items.init))
  override def toString: String = path
  private def makePath(list: Seq[String]): String = list.mkString("/", "/", "")
}


class MemoryFileSystem(val root: File, matchedFile: File => Boolean = _ => true) {
  require(root.exists())

  var _tree = walk(root)

  def tree: MemoryFileNode = _tree

  private def walk(file: File): MemoryFileNode = {
    new MemoryFileNode(new InMemoryFilePath("/"), file.isDirectory, getContentOf(file),
      Option(file.listFiles()).map(_.filter(matchedFile)).map(_.toList.map(walk)).getOrElse(Nil))
  }

  private def getContentOf(file: File): Option[Content] = {
    Option(file).filter(_.isFile).map(f => Content(FileUtils.readFileToString(file, "UTF-8"), "UTF-8"))
  }

  def findOrCreateDir(path: InMemoryFilePath): MemoryFile = if (path.isRoot) rootDir
  else {
    val parent = findOrCreateDir(path.parent)
    if (!parent.isDirectory) throw new Exception("Directory required: " + parent.path)
    findChild(parent._path, path.filename) match {
      case Some(f) => f
      case _ => createChildDirectory(parent._path, path.filename).get
    }
  }

  def findOrCreateFile(path: InMemoryFilePath): MemoryFile = {
    if (path.isRoot) {
      rootDir
    } else {
      val parent = findOrCreateDir(path.parent)
      if (!parent.isDirectory) {
        throw new Exception("Directory required: " + parent.path)
      }
      findChild(parent._path, path.filename) match {
        case Some(f) => f
        case _ => createChildFile(parent._path, path.filename).get
      }
    }
  }

  def createFile(path: InMemoryFilePath): MemoryFile = {
    path.items.tail.foldLeft(new InMemoryFilePath(path.items.head)) {
      case (p, item) => val newPath = p.child(item)
        findFile(newPath) match {
          case Some(f) => newPath
          case _ =>
            createChildDirectory(p, item)
            newPath
        }
    }
    findFile(path).get
  }


  def isDirectory(path: InMemoryFilePath): Option[Boolean] = findNodeByPath(path).map(_.isDirectory)

  def contentOf(path: InMemoryFilePath): Option[Content] = findNodeByPath(path).flatMap(_.content)

  val rootPath = InMemoryFilePath("/")

  private def createChild(path: InMemoryFilePath, newFileNode: MemoryFileNode): Option[MemoryFile] = {
    if (path == rootPath) {
      _tree = _tree.copy(children = _tree.children :+ newFileNode)
      Some(toFile(path.child(newFileNode.name)))
    } else {
      findNodeByPath(path) match {
        case Some(node) if node.isDirectory =>
          _tree = transform(_tree, {
            case n if n == node => Seq(n.copy(children = n.children :+ newFileNode))
            case n => Seq(n)
          })
          findNodeByPath(path.child(newFileNode.name)).map(node => toFile(path.child(node.name)))
        case _ => None
      }
    }
  }

  def createChildDirectory(path: InMemoryFilePath, childName: String): Option[MemoryFile] =
    createChild(path, new MemoryFileNode(path.child(childName), true, None))

  def createChildFile(path: InMemoryFilePath, childName: String): Option[MemoryFile] = createChild(path, new MemoryFileNode(path.child(childName), false, Some(Content("", "UTF-8"))))

  def parentOf(path: InMemoryFilePath): Option[MemoryFile] = Some(toFile(path.parent))

  def delete(path: InMemoryFilePath): Unit = findNodeByPath(path) match {
    case Some(node) => _tree = if (path == rootPath)
      new MemoryFileNode(path.child(root.getName), true, None, Nil)
    else transform(_tree, {
      case n if n == node => Nil
      case n => Seq(n)
    })
    case _ => throw new FileNotFoundException("File not found: " + path)
  }

  def setContent(path: InMemoryFilePath, newContent: String): Unit = findNodeByPath(path) match {
    case Some(node) => _tree = transform(_tree, {
      case n if n == node => Seq(n.copy(content = n.content.map(_.copy(text = newContent))))
      case n => Seq(n)
    })
    case _ => throw new FileNotFoundException("File not found: " + path)
  }

  def findChild(path: InMemoryFilePath, childName: String): Option[MemoryFile] = findNodeByPath(path)
    .flatMap(_.children.find(_.name == childName))
    .map(node => toFile(path.child(node.name)))

  def findFile(path: InMemoryFilePath): Option[MemoryFile] = {
    findNodeByPath(path).map(node => toFile(path.child(node.name)))
  }

  def findNodeByPath(path: InMemoryFilePath): Option[MemoryFileNode] = {
    if (path == rootPath) Some(_tree)
    else path.items.foldLeft(Option(_tree)) {
      case (Some(node), filename) => node.children.find(_.name == filename)
      case _ => None
    }
  }

  def exists(path: InMemoryFilePath): Boolean = findNodeByPath(path).isDefined
  def move(path: InMemoryFilePath, newParentPath: InMemoryFilePath): Unit = (findNodeByPath(path), findNodeByPath(newParentPath)) match {
    case (Some(currentFile), Some(newParent)) =>
      _tree = transform(_tree, {
        case node if node == currentFile => Nil
        case node if node == newParent => Seq(newParent.copy(children = newParent.children :+ currentFile))
        case node => Seq(node)
      })
      if (newParentPath == rootPath) {
        _tree = _tree.copy(children = _tree.children :+ currentFile)
      }
    case (None, _) => throw new Exception("File is not found: " + path)
    case (_, None) => throw new Exception("New parent is not found: " + newParentPath)
  }

  def childrenOf(path: InMemoryFilePath): Seq[MemoryFile] = findNodeByPath(path).map(_.children).getOrElse(Nil).map(node => toFile(path.child(node.name)))

  def rename(path: InMemoryFilePath, newName: String): Unit = findNodeByPath(path) match {
    case Some(node) => _tree = transform(_tree, {
      case n if n == node => Seq(node.copy(path = path.child(newName)))
      case n => Seq(n)
    })
    case _ => throw new Exception("File is not found: " + path)
  }

  def rootDir: MemoryFile = toFile(rootPath)

  def allFiles: Seq[String] = {
    def collect(f: MemoryFileNode): List[String] = f.name :: f.children.flatMap(collect)
    collect(_tree)
  }

  private def transform(tree: MemoryFileNode, transformChildNode: MemoryFileNode => Seq[MemoryFileNode]): MemoryFileNode = {
    tree.copy(children = tree.children.flatMap(transformChildNode).map(transform(_, transformChildNode)))
  }

  private def toFile(path: InMemoryFilePath): MemoryFile = {
    new MemoryFile(path, this)
  }

}

case class MemoryFileNode(path: InMemoryFilePath, isDirectory: Boolean, content: Option[Content], children: List[MemoryFileNode] = Nil) {
  def name: String = path.filename
}

case class MemoryFile(var _path: InMemoryFilePath, fs: MemoryFileSystem) extends MyFile {
  override def exists: Boolean = fs.exists(_path)
  override def move(newParent: MyFile): Unit = fs.move(_path, newParent.asInstanceOf[MemoryFile]._path)
  override def children: Seq[MyFile] = fs.childrenOf(_path)
  override def rename(newName: String): Unit = {
    fs.rename(_path, newName)
    _path = _path.rename(newName)
  }
  override def findChild(name: String): Option[MyFile] = fs.findChild(_path, name)
  override def setContent(newContent: String): Unit = fs.setContent(_path, newContent)
  override def documentContent: Option[Content] = ???
  override def summary: Option[FileSummary] = Some(new FileSummary(path, (new Md5).apply(content.text)))
  override def name: String = _path.filename
  override def delete(): Unit = fs.delete(_path)
  override def relativePath: Option[String] = _path.relativeTo(new InMemoryFilePath(fs.root.getPath))
  override def isBinary: Boolean = Seq(".jar", ".class").exists(_path.filename.endsWith)
  override def isDirectory: Boolean = fs.isDirectory(_path).get
  override def content: Content = fs.contentOf(_path).get
  override def createChildDirectory(name: String): MyFile = fs.createChildDirectory(_path, name).getOrElse(throw new Exception("Dir not found: " + _path))
  override def isChildOf(parent: MyFile): Boolean = _path.parent == parent.asInstanceOf[MemoryFile]._path
  override def parent: MyFile = fs.parentOf(_path).get
  override def path: String = _path.toString

}
