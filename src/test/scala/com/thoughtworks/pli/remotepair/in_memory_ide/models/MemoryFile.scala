package com.thoughtworks.pli.remotepair.in_memory_ide.models

import java.io.File

import com.thoughtworks.pli.intellij.remotepair.protocol.{FileSummary, Content}
import com.thoughtworks.pli.intellij.remotepair.utils.Md5
import com.thoughtworks.pli.remotepair.core.models.MyFile
import org.apache.commons.io.FileUtils

class Path(path: String) {
  def relativeTo(base: Path): Option[String] = base.items.foldLeft(Option(this)) {
    case (Some(result), item) if result.items.startsWith(item) => Some(new Path(result.items.tail.mkString("/")))
    case _ => None
  }.map(_.toString)

  def isRoot: Boolean = path.isEmpty
  def filename: String = items.last
  def items: Seq[String] = path.split("/")
  def rename(newName: String) = new Path((items.init :+ newName).mkString("/"))
  def child(fileName: String) = new Path((items :+ fileName).mkString("/"))
  def parent = new Path(items.init.mkString("/"))
  override def toString: String = path
}

class MemoryFileSystem(val root: File) {

  var tree = walk(root)

  private def walk(file: File): MemoryFileNode = {
    new MemoryFileNode(file.getName, file.isDirectory, getContentOf(file), file.listFiles().map(walk))
  }

  private def getContentOf(file: File): Option[Content] = {
    Option(file).filter(_.isFile).map(f => Content(FileUtils.readFileToString(file, "UTF-8"), "UTF-8"))
  }

  def findOrCreateDir(path: Path): MemoryFile = {
    if (path.isRoot) {
      rootFile
    } else {
      val parent = findOrCreateDir(path.parent)
      if (!parent.isDirectory) {
        throw new Exception("Directory required: " + parent.path)
      }
      findChild(parent._path, path.filename) match {
        case Some(f) => f
        case _ => createChildDirectory(parent._path, path.filename).get
      }
    }
  }

  def findOrCreateFile(path: Path): MemoryFile = {
    if (path.isRoot) {
      rootFile
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

  def createFile(path: Path): MemoryFile = {
    path.items.tail.foldLeft(new Path(path.items.head)) {
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


  def isDirectory(path: Path): Option[Boolean] = findNodeByPath(path).map(_.isDirectory)

  def contentOf(path: Path): Option[Content] = findNodeByPath(path).map(node => toFile(path.child(node.name))).map(_.content)

  private def createChild(path: Path, newFileNode: MemoryFileNode): Option[MemoryFile] = findNodeByPath(path) match {
    case Some(node) if !node.isDirectory =>
      tree = transform(tree, {
        case n if n == node => Seq(n.copy(children = n.children :+ newFileNode))
        case n => Seq(n)
      })
      findNodeByPath(path.child(newFileNode.name)).map(node => toFile(path.child(node.name)))
    case _ => None
  }

  def createChildDirectory(path: Path, childName: String): Option[MemoryFile] = createChild(path, new MemoryFileNode(childName, true, None))

  def createChildFile(path: Path, childName: String): Option[MemoryFile] = createChild(path, new MemoryFileNode(childName, false, Some(Content("", "UTF-8"))))

  def parentOf(path: Path): Option[MemoryFile] = findNodeByPath(path.parent).map(node => toFile(path.child(node.name)))

  def delete(path: Path): Unit = findNodeByPath(path) match {
    case Some(node) => transform(tree, {
      case n if n == node => Nil
      case n => Seq(n)
    })
    case _ => throw new Exception("File not found: " + path)
  }

  def setContent(path: Path, newContent: String): Unit = findNodeByPath(path) match {
    case Some(node) => transform(tree, {
      case n if n == node => Seq(n.copy(content = n.content.map(_.copy(text = newContent))))
      case n => Seq(n)
    })
    case _ => throw new Exception("File not found: " + path)
  }

  def findChild(path: Path, childName: String): Option[MemoryFile] = findNodeByPath(path).flatMap(_.children.find(_.name == childName)).map(node => toFile(path.child(node.name)))

  def findFile(path: Path): Option[MemoryFile] = {
    findNodeByPath(path).map(node => toFile(path.child(node.name)))
  }

  def findNodeByPath(path: Path): Option[MemoryFileNode] = {
    path.items.foldLeft(Option(tree)) {
      case (Some(node), filename) => node.children.find(_.name == filename)
      case _ => None
    }
  }

  def exists(path: Path): Boolean = findNodeByPath(path).isDefined
  def move(path: Path, newParentPath: Path): Unit = (findNodeByPath(path), findNodeByPath(newParentPath)) match {
    case (Some(currentFile), Some(newParent)) =>
      tree = transform(tree, {
        case node if node == currentFile => Nil
        case node if node == newParent => Seq(newParent.copy(children = newParent.children :+ currentFile))
        case node => Seq(node)
      })
    case (None, _) => throw new Exception("File is not found: " + path)
    case (_, None) => throw new Exception("New parent is not found: " + newParentPath)
  }

  def childrenOf(path: Path): Seq[MemoryFile] = findNodeByPath(path).map(_.children).getOrElse(Nil).map(node => toFile(path.child(node.name)))

  def rename(path: Path, newName: String): Unit = findNodeByPath(path) match {
    case Some(node) => tree = transform(tree, {
      case n if n == node => Seq(node.copy(name = newName))
      case n => Seq(n)
    })
    case _ => throw new Exception("File is not found: " + path)
  }

  def rootFile = toFile(new Path(root.getPath))

  private def transform(tree: MemoryFileNode, transformChildNode: MemoryFileNode => Seq[MemoryFileNode]): MemoryFileNode = {
    tree.copy(children = tree.children.flatMap(transformChildNode).map(transform(_, transformChildNode)))
  }

  private def toFile(path: Path): MemoryFile = {
    new MemoryFile(path, this)
  }

}

case class MemoryFileNode(name: String, isDirectory: Boolean, content: Option[Content], children: Seq[MemoryFileNode] = Nil)

case class MemoryFile(var _path: Path, fs: MemoryFileSystem) extends MyFile {
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
  override def name: String = path.split("/").toList.last
  override def delete(): Unit = fs.delete(_path)
  override def relativePath: Option[String] = _path.relativeTo(new Path(fs.root.getPath))
  override def isBinary: Boolean = Seq(".jar", ".class").exists(_path.filename.endsWith)
  override def isDirectory: Boolean = fs.isDirectory(_path).get
  override def content: Content = fs.contentOf(_path).get
  override def createChildDirectory(name: String): MyFile = fs.createChildDirectory(_path, name).getOrElse(throw new Exception("Dir not found: " + _path))
  override def isChildOf(parent: MyFile): Boolean = _path.parent == parent.asInstanceOf[MemoryFile]._path
  override def parent: MyFile = fs.parentOf(_path).get
  override def path: String = _path.toString

}
