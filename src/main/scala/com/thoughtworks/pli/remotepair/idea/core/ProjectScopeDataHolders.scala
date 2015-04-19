package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.util.Key
import com.thoughtworks.pli.intellij.remotepair.protocol.{ClientInfoResponse, ServerStatusResponse}
import com.thoughtworks.pli.intellij.remotepair.server.Server

trait CurrentProjectDataHolder[T] {
  val notifyChanges: NotifyChanges
  val currentProjectScope: CurrentProjectScope
  val key: Key[Option[T]]
  private lazy val dataHolder = currentProjectScope.value(key, None)
  def get: Option[T] = dataHolder.get
  def put(server: Option[T]) = {
    this.dataHolder.set(server)
    notifyChanges()
  }
}

object CurrentProjectDataHolderKeys {
  val ServerStatusHolder = new Key[Option[ServerStatusResponse]](this.getClass.getName)
  val ClientInfoHolder = new Key[Option[ClientInfoResponse]](this.getClass.getName)
  val ServerHolderKey = new Key[Option[Server]](this.getClass.getName)
  val ConnectionHolder = new Key[Option[Connection]](this.getClass.getName)
  val ChannelHandlerHolder = new Key[Option[MyChannelHandler]](this.getClass.getName)
}

class ServerStatusHolder(val notifyChanges: NotifyChanges, val currentProjectScope: CurrentProjectScope) extends CurrentProjectDataHolder[ServerStatusResponse] {
  override val key = CurrentProjectDataHolderKeys.ServerStatusHolder
}

class ClientInfoHolder(val notifyChanges: NotifyChanges, val currentProjectScope: CurrentProjectScope) extends CurrentProjectDataHolder[ClientInfoResponse] {
  override val key = CurrentProjectDataHolderKeys.ClientInfoHolder
}

class ServerHolder(val notifyChanges: NotifyChanges, val currentProjectScope: CurrentProjectScope) extends CurrentProjectDataHolder[Server] {
  override val key = CurrentProjectDataHolderKeys.ServerHolderKey
}

class ConnectionHolder(val notifyChanges: NotifyChanges, val currentProjectScope: CurrentProjectScope) extends CurrentProjectDataHolder[Connection] {
  override val key = CurrentProjectDataHolderKeys.ConnectionHolder
}

class ChannelHandlerHolder(val notifyChanges: NotifyChanges, val currentProjectScope: CurrentProjectScope) extends CurrentProjectDataHolder[MyChannelHandler] {
  override val key = CurrentProjectDataHolderKeys.ChannelHandlerHolder
}
