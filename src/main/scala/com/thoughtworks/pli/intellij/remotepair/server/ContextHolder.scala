package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel.ChannelHandlerContext
import scala.collection.mutable

trait WorkingModeGroups {
  def caretSharingModeGroups: List[Set[String]] = AppObjects.caretSharingModeGroups

  def caretSharingModeGroups_=(groups: List[Set[String]]) = AppObjects.caretSharingModeGroups = groups

  def followModeMap: Map[String, Set[String]] = AppObjects.followModeMap

  def followModeMap_=(map: Map[String, Set[String]]) = AppObjects.followModeMap = map

  // FIXME not implemented yet
  def parallelModeClients: Set[String] = AppObjects.parallelClients

  def parallelModeClients_=(clients: Set[String]) = AppObjects.parallelClients = clients
}

trait ProjectsHolder {
  def projects: Map[String, Project] = AppObjects.projects

  def projects_=(projects: Map[String, Project]) = AppObjects.projects = projects
}

case class Project(name: String, members: Set[String], ignoredFiles: Seq[String])

trait ContextInitializer {
  def contexts: mutable.Map[ChannelHandlerContext, ContextData]
}

trait ContextHolder {
  def contexts: Contexts = new Contexts {
    override val contexts = AppObjects.contexts
  }
}

trait Contexts {
  val contexts: mutable.Map[ChannelHandlerContext, ContextData]

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
