package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel.ChannelHandlerContext
import scala.collection.mutable

trait ContextHolderProvider {
  val contexts: ContextHolder
  var ignoredFiles: Seq[String] = Nil
}

trait ClientModeGroups {
  var bindModeGroups: List[Set[String]] = Nil
  var followModeMap: Map[String, Set[String]] = Map.empty
}

trait ProjectsHolder {
  var projects: Map[String, Set[String]] = Map.empty
}

class ContextHolder {

  val contexts = mutable.LinkedHashMap.empty[ChannelHandlerContext, ContextData]

  def add(context: ChannelHandlerContext): ContextData = {
    val data = new ContextData(context)
    contexts.put(context, data)
    data
  }

  def contains(context: ChannelHandlerContext): Boolean = contexts.contains(context)

  def remove(context: ChannelHandlerContext) {
    contexts.remove(context)
  }

  def all = contexts.values.toList

  def get(context: ChannelHandlerContext): Option[ContextData] = contexts.get(context)

  def size = contexts.size

}
