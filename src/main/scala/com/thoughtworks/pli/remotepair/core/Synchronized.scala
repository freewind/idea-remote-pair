package com.thoughtworks.pli.remotepair.core

class Synchronized {
  def apply[T](obj: AnyRef)(f: => T) = obj.synchronized(f)
}
