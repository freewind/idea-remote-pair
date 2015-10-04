package com.thoughtworks.pli.remotepair.in_memory_ide.models

import java.io.{FileNotFoundException, File}

import com.thoughtworks.pli.intellij.remotepair.protocol.Content
import org.specs2.mutable.Specification

class MemoryFileSystemSpec extends Specification {
  isolated

  "MemoryFileSystem for the testing directory" should {
    def normalFile(file: File) = !file.getName.startsWith(".")
    val fs = new MemoryFileSystem(new File("./src/test/resources/test-project-root"), normalFile)

    "have a directory root" in {
      fs.rootDir.name === "/"
      fs.rootDir.isDirectory === true
    }
    "have 2 children" in {
      fs.tree.children.size === 2
      fs.tree.children.map(f => (f.name, f.isDirectory)).sorted === Seq(("README.md", false), ("src", true))
    }
    "have correct directories and files" in {
      def getAllFilesNames(node: MemoryFileNode): List[String] = {
        node.name :: node.children.flatMap(getAllFilesNames)
      }
      getAllFilesNames(fs.tree).sorted === List("MyJava1.java", "MyScala1.scala", "MyTestJava1.java", "MyTestScala1.scala", "README.md", "java", "java", "main", "scala", "scala", "src", "test", "test-project-root")
    }
    "find an existing file" in {
      fs.findFile(new InMemoryFilePath("/src/main/scala/MyScala1.scala")).map(_.name) === Some("MyScala1.scala")
    }
    "not find an non-existing file" in {
      fs.findFile(new InMemoryFilePath("/non-existing-file")).map(_.name) === None
    }
    "find the root" in {
      fs.findOrCreateDir(new InMemoryFilePath("/")) == fs.rootDir
    }
    "find an existing file" in {
      val file = fs.findOrCreateFile(new InMemoryFilePath("/src/main/scala/MyScala1.scala"))
      file.exists === true
      file.name === "MyScala1.scala"
      file.isDirectory === false
    }
    "create an existing file" in {
      val file = fs.findOrCreateFile(InMemoryFilePath("/aaa/bbb/ccc.txt"))
      file.exists === true
      file.name === "ccc.txt"
      file.isDirectory === false
    }
    "find an existing dir" in {
      val dir = fs.findOrCreateDir(new InMemoryFilePath("/src/main/scala"))
      dir.exists === true
      dir.isDirectory === true
      dir.name === "scala"
    }
    "creating an dir" in {
      val dir = fs.findOrCreateDir(new InMemoryFilePath("/src/main/to-create"))
      dir.exists === true
      dir.isDirectory === true
      dir.name === "to-create"
    }
    "check if a directory" in {
      fs.isDirectory(new InMemoryFilePath("/src/main")) === Some(true)
      fs.isDirectory(new InMemoryFilePath("/src/main/scala/MyScala1.scala")) === Some(false)
      fs.isDirectory(new InMemoryFilePath("/src/non-existing")) === None
    }
    "get content of a file" in {
      fs.contentOf(new InMemoryFilePath("/src/main/scala/MyScala1.scala")) === Some(Content("class MyScala1 {\n\n}\n", "UTF-8"))
    }
    "get parent of a file" in {
      fs.parentOf(InMemoryFilePath("/src/main/scala")) ==== Some(new MemoryFile(InMemoryFilePath("/src/main"), fs))
    }
    "get parent of the root" in {
      fs.parentOf(fs.rootPath) ==== Some(new MemoryFile(fs.rootPath, fs))
    }
    "delete a dir" in {
      fs.delete(InMemoryFilePath("/src/main"))
      fs.findFile(InMemoryFilePath("/src/main")) === None
    }
    "delete a file" in {
      fs.delete(InMemoryFilePath("/src/main/scala/MyScala1.scala"))
      fs.findFile(InMemoryFilePath("/src/main/scala/MyScala1.scala")) === None
    }
    "delete from root" in {
      fs.delete(fs.rootPath)
      fs.rootDir.name === "/"
      fs.rootDir.isDirectory === true
      fs.rootDir.children === Nil
    }
    "cause exception when delete non-existing file" in {
      fs.delete(InMemoryFilePath("/non-existing-file")) should throwA[FileNotFoundException]
    }
    "set content to a file" in {
      fs.setContent(InMemoryFilePath("/src/main/scala/MyScala1.scala"), "new-content")
      fs.contentOf(InMemoryFilePath("/src/main/scala/MyScala1.scala")) ==== Some(Content("new-content", "UTF-8"))
    }
    "cause exception when set content to a non-existing file will" in {
      fs.setContent(InMemoryFilePath("/non-existing-file"), "new-content") should throwA[FileNotFoundException]
    }
    "check existence of a file" in {
      fs.exists(fs.rootPath) === true
      fs.exists(InMemoryFilePath("/src/main/scala/MyScala1.scala")) === true
      fs.exists(InMemoryFilePath("/non-existing-file")) === false
    }
    "move an existing file to root" in {
      fs.move(InMemoryFilePath("/src/main/scala/MyScala1.scala"), fs.rootPath)
      fs.exists(InMemoryFilePath("/src/main/scala/MyScala1.scala")) === false
      fs.exists(InMemoryFilePath("/MyScala1.scala")) === true
    }
    "move an existing file to another dir" in {
      fs.move(InMemoryFilePath("/src/main/scala/MyScala1.scala"), InMemoryFilePath("/src"))
      fs.allFiles.foreach(println)
      fs.exists(InMemoryFilePath("/src/main/scala/MyScala1.scala")) === false
      fs.exists(InMemoryFilePath("/src/MyScala1.scala")) === true
    }
  }

}
