package com.thoughtworks.pli.remotepair.idea.core

case class GetExistingProjects(serverStatus: ServerStatusHolder) {
  def apply(): Seq[ProjectWithMemberNames] = serverStatus.get.toSeq
    .flatMap(_.projects.map(p => ProjectWithMemberNames(p.name, p.clients.map(_.name))))
}
