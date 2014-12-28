package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.project.Project

object Projects {
  private var projects = Map.empty[Project, RichProject]
  def init(project: Project): RichProject = this.synchronized {
    projects.get(project) match {
      case Some(rich) => rich
      case _ => val rich = new RichProject(project)
        projects += project -> rich
        rich
    }
  }
  def remove(project: Project): Unit = this.synchronized {
    projects -= project
  }
}
