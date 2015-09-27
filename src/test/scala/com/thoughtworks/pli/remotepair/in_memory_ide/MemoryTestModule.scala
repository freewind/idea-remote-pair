package com.thoughtworks.pli.remotepair.in_memory_ide

import java.io.File

import com.thoughtworks.pli.remotepair.in_memory_ide.models.{MemoryProject, MemoryFileSystem, MemoryIde}

trait MemoryTestModule {

  val projectRoot: File
  lazy val fs = new MemoryFileSystem(projectRoot)
  lazy val project = new MemoryProject(fs)
  lazy val memoryIde = new MemoryIde(project)

}
