package com.thoughtworks.pli.remotepair.core

import com.intellij.openapi.util.{Key, UserDataHolder}

class GetUserData {
  def apply[T](dataHolder: UserDataHolder, key: Key[T]): Option[T] = Option(dataHolder.getUserData(key))
}
