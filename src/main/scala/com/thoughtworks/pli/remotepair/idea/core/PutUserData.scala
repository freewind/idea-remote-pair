package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.util.{Key, UserDataHolder}

class PutUserData {
  def apply[T](dataHolder: UserDataHolder, key: Key[T], value: T): Unit = dataHolder.putUserData(key, value)
}
