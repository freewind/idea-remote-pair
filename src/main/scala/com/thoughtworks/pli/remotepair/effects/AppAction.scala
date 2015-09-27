package com.thoughtworks.pli.remotepair.effects

sealed trait AppAction[A]

case class LogInfo(message: String) extends AppAction[Unit]
