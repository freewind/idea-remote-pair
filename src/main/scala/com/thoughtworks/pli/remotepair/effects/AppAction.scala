package com.thoughtworks.pli.remotepair.effects

import com.thoughtworks.pli.remotepair.core.models.MyProject

sealed trait AppAction[A]

case class LogInfo(message: String) extends AppAction[Unit]

case object GetCurrentProject extends AppAction[MyProject]
