package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath

class RemoveDuplicatePaths(isSubPath: IsSubPath) {
  def apply(paths: Seq[String]): Seq[String] = {
    paths.foldLeft(List.empty[String]) {
      case (result, item) => result.headOption match {
        case Some(prev) => if (isSubPath(item, prev)) result else item :: result
        case _ => item :: result
      }
    }.reverse
  }
}
