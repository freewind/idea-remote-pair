package com.thoughtworks.pli.remotepair.in_memory_ide.models

import com.thoughtworks.pli.remotepair.core.models.MyIde

class MemoryIde(project: MemoryProject) extends MyIde {

  override def invokeLater(f: => Any): Unit = f
  override def runWriteAction(f: => Any): Unit = f
  override def runReadAction(f: => Any): Unit = f

}
