package com.thoughtworks.pli.remotepair.effects

import com.thoughtworks.pli.remotepair.core.models.MyProject

import scalaz._

object Script {

  def getCurrentProject: Script[MyProject] = toScript(GetCurrentProject)
  def logInfo(message: String): Script[Unit] = toScript(LogInfo(message))
  def pure[A](a: A): Script[A] = Monad[Script].pure(a)

  private def toScript[A](appAction: AppAction[A]): Script[A] = Free.liftFC(appAction)

}
