package com.thoughtworks.pli.remotepair.idea.core

class Synchronized {
  def apply[T](obj: AnyRef)(f: => T) = obj.synchronized(f)
}
