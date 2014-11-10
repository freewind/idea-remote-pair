package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel.ChannelHandlerContext
import scala.collection.mutable

trait ContextInitializer {
  def contexts: mutable.Map[ChannelHandlerContext, ContextData]
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
