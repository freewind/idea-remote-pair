package com.thoughtworks.pli.remotepair.core

import com.thoughtworks.pli.intellij.remotepair.protocol.{ClientInfoResponse, ServerStatusResponse}
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.remotepair.core.client.{Connection, MyChannelHandler}
import com.thoughtworks.pli.remotepair.core.models.MyProject.ProjectKey

trait CurrentProjectDataHolder[T] {
  val notifyChanges: NotifyChanges
  val currentProjectScope: CurrentProjectScope
  val key: ProjectKey[Option[T]]
  private lazy val dataHolder = currentProjectScope.value(key, None)
  def get: Option[T] = dataHolder.get
  def put(value: Option[T]) = {
    this.dataHolder.set(value)
    notifyChanges()
  }
}

object CurrentProjectDataHolderKeys {
  val ServerStatusHolder = new ProjectKey[Option[ServerStatusResponse]](classOf[ServerStatusResponse].toString)
  val ClientInfoHolder = new ProjectKey[Option[ClientInfoResponse]](classOf[ClientInfoResponse].toString)
  val ServerHolderKey = new ProjectKey[Option[Server]](classOf[Server].toString)
  val ConnectionHolder = new ProjectKey[Option[Connection]](classOf[Connection].toString)
  val ChannelHandlerHolder = new ProjectKey[Option[MyChannelHandler]](classOf[MyChannelHandler].toString)
  val ReadonlyModeHolder = new ProjectKey[Option[Boolean]]("ReadonlyMode")
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

class ReadonlyModeHolder(val notifyChanges: NotifyChanges, val currentProjectScope: CurrentProjectScope) extends CurrentProjectDataHolder[Boolean] {
  override val key = CurrentProjectDataHolderKeys.ReadonlyModeHolder
}
