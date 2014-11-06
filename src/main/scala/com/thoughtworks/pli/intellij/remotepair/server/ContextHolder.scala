package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel.ChannelHandlerContext
import scala.collection.mutable

trait WorkingModeGroups {
  def caretSharingModeGroups: List[Set[String]] = WorkingModeGroups.caretSharingModeGroups

  def caretSharingModeGroups_=(groups: List[Set[String]]) = WorkingModeGroups.caretSharingModeGroups = groups

  def followModeMap: Map[String, Set[String]] = WorkingModeGroups.followModeMap

  def followModeMap_=(map: Map[String, Set[String]]) = WorkingModeGroups.followModeMap = map

  // FIXME not implemented yet
  def parallelModeClients: Set[String] = WorkingModeGroups.parallelClients

  def parallelModeClients_=(clients: Set[String]) = WorkingModeGroups.parallelClients = clients
}

object WorkingModeGroups {
  var caretSharingModeGroups = List.empty[Set[String]]
  var followModeMap = Map.empty[String, Set[String]]
  var parallelClients = Set.empty[String]
}

trait ProjectsHolder {
  def projects: Map[String, Project] = ProjectsHolder.projects

  def projects_=(projects: Map[String, Project]) = ProjectsHolder.projects = projects
}

object ProjectsHolder {
  var projects = Map.empty[String, Project]
}

case class Project(name: String, members: Set[String], ignoredFiles: Seq[String])

trait ContextInitializer {
  def contexts: mutable.Map[ChannelHandlerContext, ContextData]
}

trait ContextHolder {
  def contexts: Contexts = Contexts
}

object Contexts extends Contexts

trait Contexts {
  val contexts: mutable.Map[ChannelHandlerContext, ContextData] = mutable.LinkedHashMap.empty[ChannelHandlerContext, ContextData]

  def add(context: ChannelHandlerContext): ContextData = {
    val data = new ContextData(context)
    contexts.put(context, data)
    data
  }

  def contains(context: ChannelHandlerContext) = contexts.contains(context)

  def remove(context: ChannelHandlerContext) {
    contexts.remove(context)
  }

  def all = contexts.values.toList

  def get(context: ChannelHandlerContext): Option[ContextData] = contexts.get(context)

  def size = contexts.size

  def findByUserName(username: String): Option[ContextData] = contexts.find(_._2.name == username).map(_._2)
}
