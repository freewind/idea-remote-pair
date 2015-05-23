package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.openapi.util.Key

object IdeaKeys {
  private var map: Map[String, Key[_]] = Map()
  def get[T](key: String): Key[T] = synchronized {
    map.get(key) match {
      case Some(ideaKey) => ideaKey.asInstanceOf[Key[T]]
      case None =>
        val ideaKey = new Key[T](key)
        map += (key -> ideaKey)
        ideaKey
    }
  }
}
