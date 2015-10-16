//package com.thoughtworks.pli.remotepair.core
//
//import com.thoughtworks.pli.remotepair.core.models.MyFile
//import com.thoughtworks.pli.remotepair.idea.MocksModule
//import com.thoughtworks.pli.remotepair.idea.file.FindOrCreateDir
//import com.thoughtworks.pli.remotepair.idea.models.MyFile
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Specification
//
//class FindOrCreateDirSpec extends Specification with Mockito with MocksModule {
//  isolated
//
//  override lazy val findOrCreateDir = new FindOrCreateDir(runtimeAssertions, getProjectBaseDir, createChildDirectory, findChild)
//
//  val root = mock[MyFile]
//  val fileAaa = mock[MyFile]
//  val fileBbb = mock[MyFile]
//
//  getProjectBaseDir.apply() returns root
//  findChild.apply(root, "aaa") returns None
//  findChild.apply(fileAaa, "bbb") returns None
//
//  createChildDirectory.apply(root, "aaa") returns fileAaa
//
//  "FindOrCreateDir" should {
//    "not create anything for '' which means the project root" in {
//      findOrCreateDir("")
//      there was no(createChildDirectory).apply(any, any)
//      there was no(findChild).apply(any, any)
//    }
//    "create dir '/aaa' if which is not exist" in {
//      findOrCreateDir("/aaa")
//      there was one(createChildDirectory).apply(root, "aaa")
//    }
//    "not create dir '/aaa' again if which is existed" in {
//      findChild.apply(root, "aaa") returns Some(fileAaa)
//      findOrCreateDir("/aaa")
//      there was no(createChildDirectory).apply(root, "aaa")
//    }
//    "create dir '/aaa/bbb' if '/aaa' is not exist" in {
//      findOrCreateDir("/aaa/bbb")
//      there was one(createChildDirectory).apply(root, "aaa")
//      there was one(createChildDirectory).apply(fileAaa, "bbb")
//    }
//    "create dir '/aaa/bbb' if '/aaa' is exist but '/aaa/bbb' is not exist" in {
//      findChild.apply(root, "aaa") returns Some(fileAaa)
//      findOrCreateDir("/aaa/bbb")
//      there was no(createChildDirectory).apply(root, "aaa")
//      there was one(createChildDirectory).apply(fileAaa, "bbb")
//    }
//    "not create anything for dir '/aaa/bbb' if '/aaa/bbb' is exist" in {
//      findChild.apply(root, "aaa") returns Some(fileAaa)
//      findChild.apply(fileAaa, "bbb") returns Some(fileBbb)
//      findOrCreateDir("/aaa/bbb")
//      there was no(createChildDirectory).apply(root, "aaa")
//      there was no(createChildDirectory).apply(fileAaa, "bbb")
//    }
//  }
//
//}
