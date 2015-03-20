package com.thoughtworks.pli.remotepair.idea.core.tree

import javax.swing.tree.TreeNode

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class CreateFileTreeNodeSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val createFileTreeNode: CreateFileTreeNode = wire[CreateFileTreeNode]

  val f1 = FakeFile(mock[VirtualFile], name = "f1", path = "/root/d1/f1")
  val f2 = FakeFile(mock[VirtualFile], name = "f2", path = "/root/d1/f2")
  val d1 = FakeDir(mock[VirtualFile], name = "d1", path = "/root/d1", children = Seq(f1, f2))
  val f3 = FakeFile(mock[VirtualFile], name = "f3", path = "/root/f3")
  val d2 = FakeDir(mock[VirtualFile], name = "d2", path = "/root/d2", children = Nil)
  val root = FakeDir(mock[VirtualFile], name = "root", path = "/root", children = Seq(d1, d2, f3))

  val files = List(f1, f2, f3, d1, d2, root)

  getRelativePath.apply(any[VirtualFile]) answers { x => files.find(_.file == x).map(_.path)}
  isDirectory.apply(any) answers { x => files.find(_.file == x).get.isInstanceOf[FakeDir]}
  getFileName.apply(any) answers { x => files.find(_.file == x).get.name}
  getFileChildren.apply(any) answers { x =>
    files.find(_.file == x).get match {
      case FakeDir(_, _, _, children) => children.map(_.file)
      case _ => Nil
    }
  }


  "CreateFileTreeNode" should {

    "generate a root node of tree which contains all the branches and leaves" in {
      val rootNode = createFileTreeNode(root.file, _ => true)
      rootNode.data.file === root.file

      rootNode.getChildCount === 3
      getNodeFile(rootNode.getChildAt(0)) === d1.file
      getNodeFile(rootNode.getChildAt(1)) === d2.file
      getNodeFile(rootNode.getChildAt(2)) === f3.file

      rootNode.getChildAt(0).getChildCount === 2
      getNodeFile(rootNode.getChildAt(0).getChildAt(0)) === f1.file
      getNodeFile(rootNode.getChildAt(0).getChildAt(1)) === f2.file
    }

    "filter the nodes when generating the tree" in {
      def filterFile(file: VirtualFile) = file == f3.file
      val rootNode = createFileTreeNode(root.file, filterFile)
      rootNode.getChildCount === 1
      getNodeFile(rootNode.getChildAt(0)) === f3.file
    }

  }

  sealed trait FakeDirOrFile {
    val file: VirtualFile
    val name: String
    val path: String
  }
  case class FakeDir(file: VirtualFile, name: String, path: String, children: Seq[FakeDirOrFile]) extends FakeDirOrFile
  case class FakeFile(file: VirtualFile, name: String, path: String) extends FakeDirOrFile

  private def getNodeFile(node: TreeNode): VirtualFile = node.asInstanceOf[FileTreeNode].data.file

}
