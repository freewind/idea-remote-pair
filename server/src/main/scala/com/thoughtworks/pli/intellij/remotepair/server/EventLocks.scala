package com.thoughtworks.pli.intellij.remotepair.server

import scala.collection.JavaConversions._
import java.util.concurrent.CopyOnWriteArrayList

class EventLocks[T] {

  private val locks = new CopyOnWriteArrayList[T]

  def add(lock: T): Unit = locks += lock

  def headOption: Option[T] = locks.headOption

  def removeHead(): Unit = if (!locks.isEmpty) locks.remove(0)

  def clear(): Unit = locks.clear()

  def size: Int = locks.size

}
