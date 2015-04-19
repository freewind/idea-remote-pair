package com.thoughtworks.pli.remotepair.idea.core

class IsReadonlyMode(readonlyModeHolder: ReadonlyModeHolder) {
  def apply(): Boolean = readonlyModeHolder.get.getOrElse(false)
}

class SetReadonlyMode(readonlyModeHolder: ReadonlyModeHolder) {
  def apply(readonly: Boolean): Unit = {
    readonlyModeHolder.put(Some(readonly))
  }
}

