package com.thoughtworks.pli.intellij

import com.intellij.openapi.project.Project
import net.liftweb.json.DefaultFormats

import scala.language.implicitConversions

package object remotepair {

  implicit val formats = DefaultFormats

}

