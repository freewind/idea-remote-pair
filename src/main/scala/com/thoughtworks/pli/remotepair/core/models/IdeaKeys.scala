package com.thoughtworks.pli.remotepair.core.models

import com.intellij.openapi.util.Key

object IdeaKeys {
  private var map: Map[String, Key[_]] = Map()
  def get[T](key: DataKey[T]): Key[T] = synchronized {
    map.get(key.id) match {
      case Some(ideaKey) => ideaKey.asInstanceOf[Key[T]]
      case None =>
        val ideaKey = new Key[T](key.id)
        map += (key.id -> ideaKey)
        ideaKey
    }
  }
}
