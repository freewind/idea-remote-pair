package com.thoughtworks.pli.remotepair.core.tree

import javax.swing.tree.TreeNode

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.MocksModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class CreateFileTreeSpec extends Specification with Mockito with MocksModule {
  isolated

  override lazy val createFileTree = new CreateFileTree(getRelativePath, isDirectory, getFileChildren, fileTreeNodeDataFactory)

  val f1 = FakeFile(mock[VirtualFile], name = "f1")
  val f2 = FakeFile(mock[VirtualFile], name = "f2")
  val d1 = FakeDir(mock[VirtualFile], name = "d1", children = Seq(f1, f2))
  val f3 = FakeFile(mock[VirtualFile], name = "f3")
  val d2 = FakeDir(mock[VirtualFile], name = "d2", children = Nil)
  val root = FakeDir(mock[VirtualFile], name = "root", children = Seq(d1, d2, f3))

  val files = List(f1, f2, f3, d1, d2, root)

  isDirectory.apply(any) answers { file => files.find(_.file == file).get.isInstanceOf[FakeDir]}
  getFileName.apply(any) answers { file => files.find(_.file == file).get.name}
  fileTreeNodeDataFactory.apply(any) answers { file => new FileTreeNodeData(files.find(_.file == file).get.file)(getFileName)}
  getFileChildren.apply(any) answers { childFile =>
    files.find(_.file == childFile).get match {
      case FakeDir(_, _, children) => children.map(_.file)
      case _ => Nil
    }
  }

  "CreateFileTree" should {

    "generate a root node of tree which contains all the branches and leaves" in {
      val rootNode = createFileTree(root.file, _ => true)
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
      val rootNode = createFileTree(root.file, filterFile)
      rootNode.getChildCount === 1
      getNodeFile(rootNode.getChildAt(0)) === f3.file
    }

  }

  sealed trait FakeDirOrFile {
    val file: VirtualFile
    val name: String
  }
  case class FakeDir(file: VirtualFile, name: String, children: Seq[FakeDirOrFile]) extends FakeDirOrFile
  case class FakeFile(file: VirtualFile, name: String) extends FakeDirOrFile

  private def getNodeFile(node: TreeNode): VirtualFile = node.asInstanceOf[FileTreeNode].data.file

}
