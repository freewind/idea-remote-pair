package com.thoughtworks.pli.remotepair

package object idea {

  def notNull(objects: AnyRef*): Unit = {
    if (objects.contains(null)) {
      throw new IllegalArgumentException("Should not pass null")
    }
  }
}
