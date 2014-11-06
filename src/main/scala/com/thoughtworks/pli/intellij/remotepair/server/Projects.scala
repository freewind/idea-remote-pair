package com.thoughtworks.pli.intellij.remotepair.server

object Projects extends Projects

trait Projects {
  private var map = Map.empty[String, Project]
  def all = map.values
  def singles = all.filter(_.members.size == 1)
  def contains(name: String): Boolean = map.contains(name)
  def inSameProject(user1: String, user2: String): Boolean = all.map(_.members).exists(m => m.contains(user1) && m.contains(user2))
  def get(projectName: String) = map.get(projectName)
  def findForUser(name: String): Option[Project] = map.values.find(_.hasMember(name))
  def createOrJoin(projectName: String, userName: String) = {
    findForUser(userName).foreach { p =>
      p.removeMember(userName)
    }
    get(projectName) match {
      case Some(p) => p.addMember(userName)
      case _ => map += (projectName -> Project(projectName, userName))
    }
    findForUser(userName).get
  }
}

case class Project(name: String, userName: String) {
  var members: Set[String] = Set(userName)
  var ignoredFiles: Seq[String] = Nil
  def hasMember(user: String): Boolean = members.contains(user)
  def addMember(user: String) {
    members = members + user
  }
  def removeMember(user: String) {
    members = members - user
  }
  def isEmpty: Boolean = members.isEmpty
}
