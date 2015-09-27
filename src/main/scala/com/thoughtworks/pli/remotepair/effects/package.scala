package com.thoughtworks.pli.remotepair

import scalaz._

package object effects {

  type ScriptImpl[A] = Coyoneda[AppAction, A]
  type Script[A] = Free[ScriptImpl, A]

  implicit class ScriptOps[A](aa: Script[A]) {
    def runWith(interpreter: Interpreter): A = Free.runFC(aa)(interpreter)
  }

}
