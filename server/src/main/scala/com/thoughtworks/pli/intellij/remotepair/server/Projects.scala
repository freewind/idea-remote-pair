package com.thoughtworks.pli.intellij.remotepair.server

import com.thoughtworks.pli.intellij.remotepair.{WorkingMode, CaretSharingModeRequest, WorkingModeEvent}

object Projects extends Projects

trait Projects {
  private var map = Map.empty[String, Project]
  def all = map.values
  def singles = all.filter(_.members.size == 1)
  def contains(projectName: String): Boolean = map.contains(projectName)
  def inSameProject(user1: ContextData, user2: ContextData): Boolean = all.map(_.members).exists(m => m.contains(user1) && m.contains(user2))
  def get(projectName: String) = map.get(projectName)
  def findForClient(context: ContextData): Option[Project] = map.values.find(_.hasMember(context))
  def createOrJoin(projectName: String, client: ContextData) = {
    findForClient(client).foreach { p =>
      p.removeMember(client)
    }
    get(projectName) match {
      case Some(p) => p.addMember(client)
      case _ => map += (projectName -> Project(projectName, client))
    }
    findForClient(client).get
  }
}

case class Project(name: String, var member: ContextData) {
  var members: Seq[ContextData] = Seq(member)
  var ignoredFiles: Seq[String] = Nil

  def hasMember(user: ContextData) = members.exists(_.name == user.name)
  def addMember(user: ContextData) {
    members = members :+ user
  }
  def removeMember(user: ContextData) {
    members = members.filterNot(_.name == user.name)
  }
  def isEmpty = members.isEmpty

  var myWorkingMode: WorkingMode.Value = WorkingMode.CaretSharing

  def isSharingCaret = myWorkingMode == WorkingMode.CaretSharing

}
