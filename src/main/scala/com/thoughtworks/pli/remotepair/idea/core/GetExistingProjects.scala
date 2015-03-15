package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory._

case class GetExistingProjects(currentProject: RichProject) {
  def apply(): Seq[ProjectWithMemberNames] = currentProject.serverStatus.toSeq
    .flatMap(_.projects.map(p => ProjectWithMemberNames(p.name, p.clients.map(_.name))))
}
