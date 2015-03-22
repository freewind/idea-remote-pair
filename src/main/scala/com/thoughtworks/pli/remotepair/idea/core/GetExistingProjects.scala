package com.thoughtworks.pli.remotepair.idea.core

case class GetExistingProjects(serverStatusHolder: ServerStatusHolder) {
  def apply(): Seq[ProjectWithMemberNames] = serverStatusHolder.get.toSeq
    .flatMap(_.projects.map(p => ProjectWithMemberNames(p.name, p.clients.map(_.name))))
}
