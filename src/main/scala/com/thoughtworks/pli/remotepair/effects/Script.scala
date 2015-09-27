package com.thoughtworks.pli.remotepair.effects

import scalaz._

object Script {

  def logInfo(message: String): Script[Unit] = toScript(LogInfo(message))
  def pure[A](a: A): Script[A] = Monad[Script].pure(a)

  private def toScript[A](appAction: AppAction[A]): Script[A] = Free.liftFC(appAction)

}
